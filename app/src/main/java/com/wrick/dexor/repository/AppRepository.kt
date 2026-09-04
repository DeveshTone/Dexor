package com.wrick.dexor.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.wrick.dexor.db.AppDao
import com.wrick.dexor.db.AppEntity
import com.wrick.dexor.model.AppInfo
import com.wrick.dexor.model.InstallSource
import com.wrick.dexor.shizuku.DexManager
import com.wrick.dexor.shizuku.ShizukuHelper
import com.wrick.dexor.ui.util.AppIconManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val appDao: AppDao
) {
    /**
     * High-speed parallel loading inspired by LibChecker:
     * - PM getInstalledPackages() and Shizuku bulk dexopt run concurrently via async/await.
     * - In-memory processing splits package mapping over IO threads.
     * - Preheats AppIconManager cache asynchronously off the main thread.
     * - Instant cached response, background DB persistence.
     */
    @Suppress("DEPRECATION")
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager

        // Step 1: Concurrently fetch packages, cached DB entities, and Shizuku bulk dexopt states
        val packagesDeferred = async { pm.getInstalledPackages(0) }
        val savedEntitiesDeferred = async { appDao.getAllApps().associateBy { it.packageName } }
        val dexoptMapDeferred = async {
            if (ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasPermission()) {
                DexManager.getAllDexoptStates()
            } else {
                emptyMap()
            }
        }

        val packages = packagesDeferred.await()
        val savedEntities = savedEntitiesDeferred.await()
        val dexoptMap = dexoptMapDeferred.await()
        val shizukuReady = dexoptMap.isNotEmpty()
        val now = System.currentTimeMillis()

        // Step 2: Process packages in parallel chunks
        val chunkSize = (packages.size / 4).coerceAtLeast(20)
        val processedChunks = packages.chunked(chunkSize).map { chunk ->
            async {
                val subList = ArrayList<AppInfo>(chunk.size)
                val subEntities = ArrayList<AppEntity>(chunk.size)

                for (pkg in chunk) {
                    val ai = pkg.applicationInfo ?: continue
                    val pkgName = pkg.packageName
                    val name = pm.getApplicationLabel(ai).toString()
                    val lastUpdate = pkg.lastUpdateTime

                    val isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val saved = savedEntities[pkgName]

                    val source = if (isSystem) {
                        InstallSource.SYSTEM
                    } else {
                        val cachedSource = saved?.installSource?.let {
                            try { InstallSource.valueOf(it) } catch (e: Exception) { null }
                        }
                        if (cachedSource != null) {
                            cachedSource
                        } else {
                            val installer = try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    pm.getInstallSourceInfo(pkgName).installingPackageName
                                } else {
                                    @Suppress("DEPRECATION")
                                    pm.getInstallerPackageName(pkgName)
                                }
                            } catch (e: Exception) { null }

                            if (installer == "com.android.vending") InstallSource.GOOGLE_PLAY else InstallSource.USER
                        }
                    }

                    val dexState = dexoptMap[pkgName]
                    val status = dexState?.status ?: (if (shizukuReady) "unknown" else (saved?.lastKnownDexStatus ?: "N/A"))
                    val reason = dexState?.reason ?: (if (shizukuReady) "unknown" else (saved?.lastKnownDexReason ?: "N/A"))

                    var statusChangeTimestamp = saved?.statusChangeTimestamp

                    if (saved != null) {
                        if (saved.lastUpdateTime != lastUpdate) {
                            AppIconManager.invalidatePackage(context, pkgName)
                        }
                        if (saved.lastKnownDexStatus != null && saved.lastKnownDexStatus != status && status != "N/A") {
                            statusChangeTimestamp = now
                        }
                    }

                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pkg.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        pkg.versionCode.toLong()
                    }

                    subList.add(
                        AppInfo(
                            name = name,
                            packageName = pkgName,
                            source = source,
                            dexStatus = status,
                            dexReason = reason,
                            lastUpdateTime = lastUpdate,
                            lastStatusChangeTime = statusChangeTimestamp,
                            lastCompilationTime = saved?.lastCompilationTimestamp,
                            versionName = pkg.versionName ?: "N/A",
                            versionCode = vCode,
                            targetSdk = ai.targetSdkVersion,
                            minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ai.minSdkVersion else null,
                            codePath = ai.sourceDir,
                            dataDir = ai.dataDir
                        )
                    )

                    subEntities.add(
                        AppEntity(
                            packageName = pkgName,
                            lastKnownDexStatus = if (status != "N/A") status else saved?.lastKnownDexStatus,
                            lastKnownDexReason = if (reason != "N/A") reason else saved?.lastKnownDexReason,
                            lastUpdateTime = lastUpdate,
                            lastCompilationTimestamp = saved?.lastCompilationTimestamp,
                            statusChangeTimestamp = statusChangeTimestamp ?: saved?.statusChangeTimestamp,
                            installSource = source.name
                        )
                    )
                }
                Pair(subList, subEntities)
            }
        }.awaitAll()

        val results = ArrayList<AppInfo>(packages.size)
        val entitiesToSave = ArrayList<AppEntity>(packages.size)
        for (pair in processedChunks) {
            results.addAll(pair.first)
            entitiesToSave.addAll(pair.second)
        }

        // Persist to Room in background without blocking UI list return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appDao.insertApps(entitiesToSave)
            } catch (e: Exception) {
                // Ignore background save errors
            }
        }
        val sortedResults = results.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

        // Cooperatively preheat missing icons to on-device disk storage in background
        AppIconManager.preheatDiskCache(context, sortedResults.map { it.packageName })

        sortedResults
    }

    /**
     * Query fresh details for a single package immediately after compilation or when opened.
     */
    suspend fun getFreshAppInfo(app: AppInfo): AppInfo = withContext(Dispatchers.IO) {
        val state = DexManager.getDexoptState(app.packageName)
        val now = System.currentTimeMillis()

        val newStatus = state?.status ?: app.dexStatus
        val newReason = state?.reason ?: app.dexReason
        val changed = newStatus != app.dexStatus

        val pm = context.packageManager
        val realSource = if (app.source != InstallSource.USER) {
            app.source
        } else {
            val installer = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    pm.getInstallSourceInfo(app.packageName).installingPackageName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstallerPackageName(app.packageName)
                }
            } catch (e: Exception) { null }
            if (installer == "com.android.vending") InstallSource.GOOGLE_PLAY else InstallSource.USER
        }

        val entity = appDao.getAppByPackageName(app.packageName)
        if (entity != null) {
            appDao.insertApp(
                entity.copy(
                    lastKnownDexStatus = newStatus,
                    lastKnownDexReason = newReason,
                    lastCompilationTimestamp = now,
                    statusChangeTimestamp = if (changed) now else entity.statusChangeTimestamp,
                    installSource = realSource.name
                )
            )
        }

        app.copy(
            source = realSource,
            dexStatus = newStatus,
            dexReason = newReason,
            lastCompilationTime = now,
            lastStatusChangeTime = if (changed) now else app.lastStatusChangeTime
        )
    }

    /**
     * Executes compiler mode via Shizuku shell broker.
     */
    suspend fun compileApp(packageName: String, mode: String): Result<Unit> = withContext(Dispatchers.IO) {
        val result = DexManager.compilePackage(packageName, mode)
        if (result.isSuccess) {
            val now = System.currentTimeMillis()
            val state = DexManager.getDexoptState(packageName)
            val entity = appDao.getAppByPackageName(packageName)
            if (entity != null) {
                appDao.insertApp(
                    entity.copy(
                        lastKnownDexStatus = state?.status ?: mode,
                        lastKnownDexReason = state?.reason ?: "cmdline",
                        lastCompilationTimestamp = now
                    )
                )
            }
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to optimize $packageName. Check Shizuku permissions."))
        }
    }
}

