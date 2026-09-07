package com.wrick.dexor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wrick.dexor.db.AppDatabase
import com.wrick.dexor.model.AppFilter
import com.wrick.dexor.model.AppInfo
import com.wrick.dexor.model.InstallSource
import com.wrick.dexor.model.ShizukuState
import com.wrick.dexor.model.SortMode
import com.wrick.dexor.repository.AppRepository
import com.wrick.dexor.shizuku.ShizukuHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val dao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(application, dao)
    }

    // --- Master data ---
    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())

    // --- Filter / Sort / Search ---
    private val _filter = MutableStateFlow(AppFilter.USER)
    val filter: StateFlow<AppFilter> = _filter.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.NAME)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Master filtered by search, sorted, and partitioned (computed on Dispatchers.Default) */
    private val _appLists = combine(
        _allApps, _sortMode, _searchQuery
    ) { apps, sort, query ->
        val filtered = if (query.isBlank()) {
            apps
        } else {
            val q = query.trim()
            apps.filter { app ->
                app.name.contains(q, ignoreCase = true) ||
                app.packageName.contains(q, ignoreCase = true)
            }
        }
        val comparator = when (sort) {
            SortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            SortMode.DEX_STATUS -> compareBy<AppInfo> {
                when (it.dexStatus.lowercase()) {
                    "speed", "speed-profile" -> 0
                    "everything" -> 1
                    "space" -> 2
                    "verify" -> 3
                    "run-from-apk", "interpret-only" -> 4
                    else -> 5
                }
            }.thenComparing(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
            SortMode.SOURCE -> compareBy<AppInfo> { it.source.ordinal }
                .thenComparing(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        }
        val sorted = filtered.sortedWith(comparator)
        val user = ArrayList<AppInfo>(sorted.size)
        val system = ArrayList<AppInfo>(sorted.size)
        for (app in sorted) {
            if (app.source == InstallSource.SYSTEM) {
                system.add(app)
            } else {
                user.add(app)
            }
        }
        Triple<List<AppInfo>, List<AppInfo>, List<AppInfo>>(sorted, user, system)
    }.flowOn(Dispatchers.Default).stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Triple(emptyList(), emptyList(), emptyList())
    )

    val sortedSearchedApps: StateFlow<List<AppInfo>> = _appLists.map { it.first }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userApps: StateFlow<List<AppInfo>> = _appLists.map { it.second }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val systemApps: StateFlow<List<AppInfo>> = _appLists.map { it.third }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- UI state ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shizukuState = MutableStateFlow(ShizukuState.NOT_RUNNING)
    val shizukuState: StateFlow<ShizukuState> = _shizukuState.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<String>>(emptySet())
    val selectedApps: StateFlow<Set<String>> = _selectedApps.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _batchProgress = MutableStateFlow<Triple<Int, Int, String>?>(null)
    val batchProgress: StateFlow<Triple<Int, Int, String>?> = _batchProgress.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    // Single App Detail Screen State
    private val _detailApp = MutableStateFlow<AppInfo?>(null)
    val detailApp: StateFlow<AppInfo?> = _detailApp.asStateFlow()

    private val _isCompilingDetail = MutableStateFlow(false)
    val isCompilingDetail: StateFlow<Boolean> = _isCompilingDetail.asStateFlow()

    // Settings & Permissions Dialog State
    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    fun openSettingsDialog() { _showSettingsDialog.value = true }
    fun closeSettingsDialog() { _showSettingsDialog.value = false }

    // Compatibility aliases
    val showPermissionsDialog: StateFlow<Boolean> get() = showSettingsDialog
    fun openPermissionsDialog() = openSettingsDialog()
    fun closePermissionsDialog() = closeSettingsDialog()

    private var detailJob: Job? = null

    fun openAppDetail(app: AppInfo) {
        detailJob?.cancel()
        _detailApp.value = app
        detailJob = viewModelScope.launch {
            val fresh = repository.getFreshAppInfo(app)
            if (_detailApp.value?.packageName == app.packageName) {
                _detailApp.value = fresh
            }
            updateAppInList(fresh)
        }
    }

    fun closeAppDetail() {
        detailJob?.cancel()
        _detailApp.value = null
    }

    fun clearSnack() { _snackMessage.value = null }

    // --- Actions ---

    fun refreshShizukuState() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = when {
                !ShizukuHelper.isShizukuAvailable() -> ShizukuState.NOT_RUNNING
                !ShizukuHelper.hasPermission() -> ShizukuState.NO_PERMISSION
                else -> ShizukuState.READY
            }
            _shizukuState.value = state
        }
    }

    fun requestShizukuPermission() {
        ShizukuHelper.requestPermission()
    }

    fun setFilter(f: AppFilter) { _filter.value = f }
    fun setSortMode(s: SortMode) { _sortMode.value = s }
    fun setSearchQuery(q: String) { _searchQuery.value = q }

    private var loadJob: Job? = null

    fun loadApps(forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true && !forceRefresh) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            refreshShizukuState()
            try {
                _allApps.value = repository.getInstalledApps()
            } catch (e: Exception) {
                _snackMessage.value = "Error loading apps: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshApps() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            refreshShizukuState()
            try {
                val apps = repository.getInstalledApps()
                _allApps.value = apps
                _snackMessage.value = "Refreshed ${apps.size} packages"
            } catch (e: Exception) {
                _snackMessage.value = "Refresh failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateAppInList(updatedApp: AppInfo) {
        val current = _allApps.value.toMutableList()
        val idx = current.indexOfFirst { it.packageName == updatedApp.packageName }
        if (idx != -1) {
            current[idx] = updatedApp
            _allApps.value = current
        }
    }

    // --- Selection ---
    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) _selectedApps.value = emptySet()
    }

    fun toggleAppSelection(pkg: String) {
        val cur = _selectedApps.value.toMutableSet()
        if (cur.contains(pkg)) cur.remove(pkg) else cur.add(pkg)
        _selectedApps.value = cur
        if (cur.isEmpty()) _isSelectionMode.value = false
    }

    fun selectAll(packages: Collection<String>) { _selectedApps.value = packages.toSet() }
    fun deselectAll() { _selectedApps.value = emptySet(); _isSelectionMode.value = false }

    // --- Compilation ---
    fun compileApp(packageName: String, mode: String) {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasPermission()) {
            _snackMessage.value = "Shizuku permission required to apply mode"
            _showSettingsDialog.value = true
            return
        }

        viewModelScope.launch {
            _isCompilingDetail.value = true
            _snackMessage.value = "Optimizing $packageName ($mode)..."
            val result = repository.compileApp(packageName, mode)

            val target = _allApps.value.find { it.packageName == packageName }
            if (target != null) {
                val freshApp = repository.getFreshAppInfo(target)
                updateAppInList(freshApp)
                if (_detailApp.value?.packageName == packageName) {
                    _detailApp.value = freshApp
                }
            }

            _snackMessage.value = result.fold(
                onSuccess = { "$packageName optimized ($mode)" },
                onFailure = { "Failed: ${it.message}" }
            )
            _isCompilingDetail.value = false
        }
    }

    fun compileBatch(mode: String) {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasPermission()) {
            _snackMessage.value = "Shizuku permission required to apply mode"
            _showSettingsDialog.value = true
            return
        }

        viewModelScope.launch {
            val pkgs = _selectedApps.value.toList()
            if (pkgs.isEmpty()) return@launch
            var ok = 0
            var fail = 0

            for ((i, pkg) in pkgs.withIndex()) {
                _batchProgress.value = Triple(i + 1, pkgs.size, pkg)
                val result = repository.compileApp(pkg, mode)
                if (result.isSuccess) ok++ else fail++
            }

            _batchProgress.value = null
            _isSelectionMode.value = false
            _selectedApps.value = emptySet()
            _snackMessage.value = "Batch complete: $ok optimized, $fail failed"
            _allApps.value = repository.getInstalledApps()
        }
    }
}

