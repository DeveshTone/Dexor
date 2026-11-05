package com.wrick.dexor.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuHelper {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermission() {
        try {
            if (isShizukuAvailable()) {
                Shizuku.requestPermission(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val newProcessMethod by lazy {
        Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        ).apply { isAccessible = true }
    }

    /**
     * Execute a shell command via Shizuku's private newProcess API.
     * Uses reflection since newProcess is private in Shizuku API 13.x.
     * Returns Result<String> with stdout on success or exception on failure.
     */
    suspend fun exec(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isShizukuAvailable()) {
                return@withContext Result.failure(IllegalStateException("Shizuku not running"))
            }
            if (!hasPermission()) {
                return@withContext Result.failure(SecurityException("Shizuku permission not granted"))
            }

            val process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process

            // Drain both streams concurrently to avoid blocking on a full pipe.
            val stdoutTask = async {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line).append("\n")
                    }
                    sb.toString()
                }
            }
            val stderrTask = async {
                BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            }

            val exitCode = process.waitFor()
            val stdout = stdoutTask.await()
            val stderr = stderrTask.await()

            if (exitCode == 0) {
                Result.success(stdout)
            } else {
                val output = listOf(stdout, stderr)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("\n")
                val message = if (output.isEmpty()) {
                    "Exit $exitCode"
                } else {
                    "Exit $exitCode: $output"
                }
                Result.failure(Exception(message))
            }
        } catch (e: java.lang.reflect.InvocationTargetException) {
            // Unwrap the actual exception from reflection
            val cause = e.cause ?: e
            cause.printStackTrace()
            Result.failure(cause)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /** Convenience: returns stdout or empty string on any failure */
    suspend fun execOrEmpty(command: String): String {
        return exec(command).getOrDefault("")
    }
}

