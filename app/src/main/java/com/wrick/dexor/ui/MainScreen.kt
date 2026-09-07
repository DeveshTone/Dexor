package com.wrick.dexor.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrick.dexor.R
import com.wrick.dexor.model.AppInfo
import com.wrick.dexor.model.COMPILE_MODES
import com.wrick.dexor.model.InstallSource
import com.wrick.dexor.model.ShizukuState
import com.wrick.dexor.model.SortMode
import com.wrick.dexor.ui.theme.DexorDimensions
import com.wrick.dexor.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    windowSizeClass: WindowSizeClass? = null
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val batchProgress by viewModel.batchProgress.collectAsState()
    val shizukuState by viewModel.shizukuState.collectAsState()
    val currentSort by viewModel.sortMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackMessage by viewModel.snackMessage.collectAsState()

    // Persistent scroll state and pager state for User and System tabs
    val userListState = rememberLazyListState()
    val sysListState = rememberLazyListState()
    // Hoisted at MainScreen level so tab selection is NEVER lost when navigating to/from detail screen
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    var detailOriginPage by rememberSaveable { mutableIntStateOf(0) }

    // Single app detail state
    val detailApp by viewModel.detailApp.collectAsState()
    val isCompilingDetail by viewModel.isCompilingDetail.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()

    var showBatchSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    // Settings & Permissions Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            shizukuState = shizukuState,
            onRequestPermission = { viewModel.requestShizukuPermission() },
            onRefreshShizuku = { viewModel.refreshShizukuState() },
            onDismiss = { viewModel.closeSettingsDialog() }
        )
    }

    val isTwoPane = windowSizeClass != null &&
            (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded)

    if (isTwoPane) {
        TwoPaneLayout(
            viewModel = viewModel,
            isLoading = isLoading,
            isSelectionMode = isSelectionMode,
            selectedApps = selectedApps,
            detailApp = detailApp,
            isCompilingDetail = isCompilingDetail,
            shizukuState = shizukuState,
            currentSort = currentSort,
            searchQuery = searchQuery,
            userListState = userListState,
            sysListState = sysListState,
            pagerState = pagerState,
            showSearch = showSearch,
            onToggleSearch = { showSearch = !showSearch },
            showSortMenu = showSortMenu,
            onToggleSortMenu = { showSortMenu = it },
            onOpenBatch = { showBatchSheet = true }
        )
    } else {
        // Cache the active detail app reference so the outgoing exit transition always renders AppDetailScreen
        var activeDetailApp by remember { mutableStateOf(detailApp) }
        LaunchedEffect(detailApp) {
            if (detailApp != null) {
                activeDetailApp = detailApp
            }
        }

        val isViewingDetail = detailApp != null

        AnimatedContent(
            targetState = isViewingDetail,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally(initialOffsetX = { it }) + fadeIn())
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut())
                } else {
                    (slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn())
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                }
            },
            label = "ScreenTransition"
        ) { inDetail ->
            val appToShow = if (inDetail) (detailApp ?: activeDetailApp) else null
            if (appToShow != null) {
                AppDetailScreen(
                    app = appToShow,
                    isCompiling = isCompilingDetail,
                    isShizukuReady = shizukuState == ShizukuState.READY,
                    onRequestPermission = { viewModel.requestShizukuPermission() },
                    onBack = { viewModel.closeAppDetail() },
                    onCompile = { mode -> viewModel.compileApp(appToShow.packageName, mode) },
                    showBackButton = true
                )
            } else {
                SinglePaneList(
                    viewModel = viewModel,
                    isLoading = isLoading,
                    isSelectionMode = isSelectionMode,
                    selectedApps = selectedApps,
                    shizukuState = shizukuState,
                    currentSort = currentSort,
                    searchQuery = searchQuery,
                    userListState = userListState,
                    sysListState = sysListState,
                    pagerState = pagerState,
                    onOpenDetailWithOrigin = { app, originPage ->
                        detailOriginPage = originPage
                        viewModel.openAppDetail(app)
                    },
                    showSearch = showSearch,
                    onToggleSearch = { showSearch = !showSearch },
                    showSortMenu = showSortMenu,
                    onToggleSortMenu = { showSortMenu = it },
                    onOpenBatch = { showBatchSheet = true }
                )
            }
        }
    }

    // --- Batch Modal Bottom Sheet ---
    if (showBatchSheet) {
        CompilationBottomSheet(
            appName = null,
            batchCount = selectedApps.size,
            onDismissRequest = { showBatchSheet = false },
            onCompileModeSelected = { mode ->
                showBatchSheet = false
                viewModel.compileBatch(mode)
            }
        )
    }

    // --- Batch progress dialog ---
    if (batchProgress != null) {
        val (current, total, pkg) = batchProgress!!
        val modeEst = COMPILE_MODES.firstOrNull()?.estimatedSecondsPerApp ?: 15
        val remaining = (total - current) * modeEst

        AlertDialog(
            onDismissRequest = { },
            title = { Text("Batch Optimization in Progress", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(
                        "$current of $total completed",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(pkg, style = MaterialTheme.typography.bodySmall, color = Color(0xFF90A4AE))
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = current.toFloat() / total.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2196F3)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Estimated remaining: ~${remaining}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78909C)
                    )
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF131720)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SinglePaneList(
    viewModel: MainViewModel,
    isLoading: Boolean,
    isSelectionMode: Boolean,
    selectedApps: Set<String>,
    shizukuState: ShizukuState,
    currentSort: SortMode,
    searchQuery: String,
    userListState: LazyListState,
    sysListState: LazyListState,
    pagerState: PagerState,
    onOpenDetailWithOrigin: (AppInfo, Int) -> Unit,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: (Boolean) -> Unit,
    onOpenBatch: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refreshAnim")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )

    val coroutineScope = rememberCoroutineScope()

    // Precomputed sublists from ViewModel on Dispatchers.Default (zero in-composition filtering)
    val userList by viewModel.userApps.collectAsState()
    val sysList by viewModel.systemApps.collectAsState()

    val showScrollToTop by remember(pagerState.currentPage) {
        derivedStateOf {
            val state = if (pagerState.currentPage == 0) userListState else sysListState
            state.firstVisibleItemIndex > 3
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedApps.size} Selected", style = MaterialTheme.typography.titleLarge)
                    } else {
                        Text(
                            "DEXOR",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color(0xFFECEFF1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = { viewModel.deselectAll() },
                            modifier = Modifier.size(DexorDimensions.minTouchTarget)
                        ) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        IconButton(
                            onClick = onToggleSearch,
                            modifier = Modifier.size(DexorDimensions.minTouchTarget)
                        ) {
                            Icon(Icons.Default.Search, "Search")
                        }

                        // Sort dropdown
                        Box {
                            IconButton(
                                onClick = { onToggleSortMenu(true) },
                                modifier = Modifier.size(DexorDimensions.minTouchTarget)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sort),
                                    contentDescription = "Sort Options",
                                    tint = Color(0xFFECEFF1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { onToggleSortMenu(false) }
                            ) {
                                SortMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (mode) {
                                                    SortMode.NAME -> "Sort by Name"
                                                    SortMode.DEX_STATUS -> "Sort by DEX Status"
                                                    SortMode.SOURCE -> "Sort by Source"
                                                },
                                                color = if (currentSort == mode) Color(0xFF90CAF9) else Color.White,
                                                fontWeight = if (currentSort == mode) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setSortMode(mode)
                                            onToggleSortMenu(false)
                                        }
                                    )
                                }
                            }
                        }

                        // Settings & App Info
                        IconButton(
                            onClick = { viewModel.openSettingsDialog() },
                            modifier = Modifier.size(DexorDimensions.minTouchTarget)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings & About")
                        }

                        // Fast Scan Refresh with smooth rotating feedback
                        IconButton(
                            onClick = { viewModel.refreshApps() },
                            enabled = !isLoading,
                            modifier = Modifier.size(DexorDimensions.minTouchTarget)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = if (isLoading) refreshRotation else 0f
                                },
                                tint = if (isLoading) Color(0xFF90CAF9) else Color.White
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                val currentItems = if (pagerState.currentPage == 0) userList else sysList
                                viewModel.selectAll(currentItems.map { it.packageName })
                            },
                            modifier = Modifier.defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                        ) {
                            Text("All", color = Color(0xFF90CAF9), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1115),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (isSelectionMode && selectedApps.isNotEmpty()) {
                Surface(
                    color = Color(0xFF131720),
                    tonalElevation = 8.dp
                ) {
                    Button(
                        onClick = onOpenBatch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(DexorDimensions.spaceDefault)
                            .defaultMinSize(minHeight = DexorDimensions.minTouchTarget),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(DexorDimensions.cornerMedium)
                    ) {
                        Text("Batch Optimize (${selectedApps.size} apps)", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val state = if (pagerState.currentPage == 0) userListState else sysListState
                            if (state.firstVisibleItemIndex > 12) {
                                state.scrollToItem(8)
                            }
                            state.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color(0xFF090A0C)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Material 3 Styled Shizuku Authorization Banner
            ShizukuBanner(
                state = shizukuState,
                onGrant = { viewModel.requestShizukuPermission() },
                onRetry = { viewModel.refreshApps() }
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Color(0xFF2196F3),
                    trackColor = Color.Transparent
                )
            }

            // Search Bar
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DexorDimensions.spaceDefault, vertical = 6.dp),
                    placeholder = { Text("Search by name or package...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFF263238),
                        focusedContainerColor = Color(0xFF131720),
                        unfocusedContainerColor = Color(0xFF131720)
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, "Clear", tint = Color.White)
                            }
                        }
                    }
                )
            }

            // Material Design 3 Style Rounded & Coloured Header for User & System
            Surface(
                color = Color(0xFF0F1115),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DexorDimensions.spaceDefault, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tabItems = listOf(
                        Pair("User Apps", userList.size),
                        Pair("System Apps", sysList.size)
                    )

                    tabItems.forEachIndexed { index, pair ->
                        val isSelected = pagerState.currentPage == index
                        val containerBg = if (isSelected) Color(0xFF1E3A5F) else Color(0xFF141A24)
                        val contentColor = if (isSelected) Color(0xFF90CAF9) else Color(0xFF78909C)
                        val borderColor = if (isSelected) Color(0xFF2196F3) else Color.Transparent

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(21.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            shape = RoundedCornerShape(21.dp),
                            color = containerBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFF2196F3).copy(alpha = 0.35f) else Color(0xFF1E2430)
                                ) {
                                    Text(
                                        text = "${pair.second}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color(0xFFE3F2FD) else Color(0xFF90A4AE),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Horizontally Swipable Pager with 2 tabs: User (0) & System (1)
            HorizontalPager(
                state = pagerState,
                beyondBoundsPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val listForPage = if (page == 0) userList else sysList
                val listStateForPage = if (page == 0) userListState else sysListState

                if (isLoading && listForPage.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF90CAF9))
                            Spacer(Modifier.height(DexorDimensions.spaceMedium))
                            Text("Loading packages...", color = Color(0xFF90A4AE), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else if (listForPage.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No matching packages found", color = Color(0xFF78909C), style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        state = listStateForPage,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = listForPage,
                            key = { it.packageName },
                            contentType = { "app_row" }
                        ) { app ->
                            val pkgName = app.packageName
                            val isSelected = selectedApps.contains(pkgName)
                            AppListItem(
                                app = app,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = remember(pkgName, isSelectionMode, page) {
                                    {
                                        if (isSelectionMode) {
                                            viewModel.toggleAppSelection(pkgName)
                                        } else {
                                            onOpenDetailWithOrigin(app, page)
                                        }
                                    }
                                },
                                onLongClick = remember(pkgName, isSelectionMode) {
                                    {
                                        if (!isSelectionMode) {
                                            viewModel.toggleSelectionMode()
                                            viewModel.toggleAppSelection(pkgName)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TwoPaneLayout(
    viewModel: MainViewModel,
    isLoading: Boolean,
    isSelectionMode: Boolean,
    selectedApps: Set<String>,
    detailApp: AppInfo?,
    isCompilingDetail: Boolean,
    shizukuState: ShizukuState,
    currentSort: SortMode,
    searchQuery: String,
    userListState: LazyListState,
    sysListState: LazyListState,
    pagerState: PagerState,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    showSortMenu: Boolean,
    onToggleSortMenu: (Boolean) -> Unit,
    onOpenBatch: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            SinglePaneList(
                viewModel = viewModel,
                isLoading = isLoading,
                isSelectionMode = isSelectionMode,
                selectedApps = selectedApps,
                shizukuState = shizukuState,
                currentSort = currentSort,
                searchQuery = searchQuery,
                userListState = userListState,
                sysListState = sysListState,
                pagerState = pagerState,
                onOpenDetailWithOrigin = { app, _ -> viewModel.openAppDetail(app) },
                showSearch = showSearch,
                onToggleSearch = onToggleSearch,
                showSortMenu = showSortMenu,
                onToggleSortMenu = onToggleSortMenu,
                onOpenBatch = onOpenBatch
            )
        }

        Divider(
            color = Color(0xFF1E2430),
            modifier = Modifier.fillMaxHeight().width(1.dp)
        )

        Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
            if (detailApp != null) {
                AppDetailScreen(
                    app = detailApp,
                    isCompiling = isCompilingDetail,
                    isShizukuReady = shizukuState == ShizukuState.READY,
                    onRequestPermission = { viewModel.requestShizukuPermission() },
                    onBack = { viewModel.closeAppDetail() },
                    onCompile = { mode -> viewModel.compileApp(detailApp.packageName, mode) },
                    showBackButton = false
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090A0C)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "Select an application to view DEX optimization details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF78909C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShizukuBanner(
    state: ShizukuState,
    onGrant: () -> Unit,
    onRetry: () -> Unit
) {
    AnimatedVisibility(
        visible = state != ShizukuState.READY,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        when (state) {
            ShizukuState.READY -> { /* clean minimal UI when ready */ }
            ShizukuState.NOT_RUNNING -> {
                Surface(
                    color = Color(0xFF2D1214),
                    shape = RoundedCornerShape(DexorDimensions.cornerMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DexorDimensions.spaceDefault, vertical = 6.dp)
                ) {
                    Row(
                        Modifier.padding(DexorDimensions.spaceMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Shizuku Not Running", color = Color(0xFFFF8A80), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(2.dp))
                            Text("Start Shizuku service, then tap Retry", color = Color(0xFFFFCDD2), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                            shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                            modifier = Modifier.defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                        ) {
                            Text("Retry", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            ShizukuState.NO_PERMISSION -> {
                Surface(
                    color = Color(0xFF2C1E12),
                    shape = RoundedCornerShape(DexorDimensions.cornerMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DexorDimensions.spaceDefault, vertical = 6.dp)
                ) {
                    Row(
                        Modifier.padding(DexorDimensions.spaceMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Shizuku Authorization Needed", color = Color(0xFFFFB74D), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(2.dp))
                            Text("Tap Grant to enable dex2oat compiling & inspection", color = Color(0xFFFFE0B2), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = onGrant,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                            modifier = Modifier.defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                        ) {
                            Text("Grant", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

