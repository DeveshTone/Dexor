package com.wrick.dexor.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.wrick.dexor.model.ShizukuState
import com.wrick.dexor.ui.theme.DexorCodeFont
import com.wrick.dexor.ui.theme.DexorDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    shizukuState: ShizukuState,
    onRequestPermission: () -> Unit,
    onRefreshShizuku: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var showLicenseText by remember { mutableStateOf(false) }
    var showFilterGuide by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & About",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(DexorDimensions.minTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1115),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF090A0C)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DexorDimensions.spaceDefault, vertical = DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceLarge)
        ) {
            // HERO APP BRAND CARD
            HeroAppCard(context = context)

            // SECTION 1: Privilege & Service
            SettingsSection(title = "SHIZUKU PRIVILEGE") {
                ShizukuStatusCard(
                    shizukuState = shizukuState,
                    onRequestPermission = onRequestPermission,
                    onRefreshShizuku = onRefreshShizuku
                )
            }

            // SECTION 2: Device & Runtime Diagnostics
            SettingsSection(title = "SYSTEM & RUNTIME ENVIRONMENT") {
                SystemEnvironmentCard()
            }

            // SECTION 3: Compilation Filters Guide
            SettingsSection(title = "DEX2OAT COMPILATION MODES") {
                CompilationGuideCard(
                    isExpanded = showFilterGuide,
                    onToggle = { showFilterGuide = !showFilterGuide }
                )
            }

            // SECTION 4: Architecture & Optimizations
            SettingsSection(title = "ENGINEERED FOR EFFICIENCY") {
                ArchitectureCard()
            }

            // SECTION 5: Privacy & Safety
            SettingsSection(title = "PRIVACY & SYSTEM INTEGRITY") {
                PrivacySafetyCard()
            }

            // SECTION 6: Open Source & Legal
            SettingsSection(title = "OPEN SOURCE & CREDITS") {
                CreditsLegalCard(
                    showLicense = showLicenseText,
                    onToggleLicense = { showLicenseText = !showLicenseText },
                    onOpenRepo = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DeveshTone/Dexor"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    },
                    onOpenReleases = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DeveshTone/Dexor/releases"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try { context.startActivity(intent) } catch (_: Exception) {}
                    }
                )
            }

            Spacer(Modifier.height(DexorDimensions.spaceLarge))
        }
    }
}

// Alias for backwards compatibility if needed
@Composable
fun SettingsDialog(
    shizukuState: ShizukuState,
    onRequestPermission: () -> Unit,
    onRefreshShizuku: () -> Unit,
    onDismiss: () -> Unit
) {
    SettingsScreen(
        shizukuState = shizukuState,
        onRequestPermission = onRequestPermission,
        onRefreshShizuku = onRefreshShizuku,
        onBack = onDismiss
    )
}

@Composable
private fun HeroAppCard(context: android.content.Context) {
    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(width = 128, height = 128).asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerLarge),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(DexorDimensions.cornerMedium),
                    color = Color(0xFF090A0C),
                    border = BorderStroke(1.dp, Color(0xFF1E2636)),
                    modifier = Modifier.size(DexorDimensions.appIconSizeDetail)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (appIconBitmap != null) {
                            Image(
                                bitmap = appIconBitmap,
                                contentDescription = "Dexor Icon",
                                modifier = Modifier.size(44.dp)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1976D2).copy(alpha = 0.25f),
                                modifier = Modifier.size(36.dp)
                            ) {}
                        }
                    }
                }

                Spacer(Modifier.width(DexorDimensions.spaceDefault))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Dexor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                            color = Color(0xFF1B5E20).copy(alpha = 0.35f),
                            border = BorderStroke(0.8.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "v1.0.0",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Unified DEX2OAT Runtime Manager",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF90A4AE)
                    )
                }
            }

            // Chips Row with proper wrapping and spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(label = "Non-Root ART", accent = Color(0xFF90CAF9))
                StatusPill(label = "3.29 MB R8", accent = Color(0xFFCE93D8))
                StatusPill(label = "100% Offline", accent = Color(0xFF81C784))
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerSmall),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(0.8.dp, accent.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ShizukuStatusCard(
    shizukuState: ShizukuState,
    onRequestPermission: () -> Unit,
    onRefreshShizuku: () -> Unit
) {
    val (statusColor, statusTitle, statusDesc) = when (shizukuState) {
        ShizukuState.READY -> Triple(
            Color(0xFF4CAF50),
            "Authorized & Ready",
            "Dexor is connected to Shizuku with elevated shell execution privileges. Native cmd package compile operations are ready."
        )
        ShizukuState.NO_PERMISSION -> Triple(
            Color(0xFFFFA726),
            "Permission Required",
            "Shizuku service is active, but Dexor has not yet been granted permission. Grant permission to enable dexopt compilation."
        )
        ShizukuState.NOT_RUNNING -> Triple(
            Color(0xFFEF5350),
            "Service Inactive",
            "Shizuku is not running. Launch the Shizuku app and start it via Wireless Debugging (ADB) or Root."
        )
    }

    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(DexorDimensions.spaceSmall))
                    Text(
                        text = "Shizuku Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = statusTitle,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = statusDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF90A4AE),
                lineHeight = 20.sp
            )

            if (shizukuState == ShizukuState.NO_PERMISSION) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                ) {
                    Text("Grant Shizuku Permission", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            } else if (shizukuState == ShizukuState.NOT_RUNNING) {
                OutlinedButton(
                    onClick = onRefreshShizuku,
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = DexorDimensions.minTouchTarget),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Recheck Shizuku Status")
                }
            }
        }
    }
}

@Composable
private fun SystemEnvironmentCard() {
    val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
    val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            MinimalInfoRow(label = "Hardware Model", value = deviceModel)
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Operating System", value = osVersion)
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "CPU Architecture", value = abi)
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Runtime Compiler", value = "Android Runtime (dex2oat)")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Backend Endpoint", value = "cmd package compile")
        }
    }
}

@Composable
private fun CompilationGuideCard(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Compiler Filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Detailed explanation of available modes",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF90A4AE)
                    )
                }

                TextButton(onClick = onToggle) {
                    Text(
                        text = if (isExpanded) "Hide" else "Show All",
                        color = Color(0xFF90CAF9),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceSmall)
                ) {
                    FilterItem(
                        mode = "speed",
                        title = "Speed (Ahead-Of-Time)",
                        desc = "Compiles all bytecode into native machine instructions ahead of time. Maximizes runtime execution velocity and eliminates JIT warmup stutter."
                    )
                    FilterItem(
                        mode = "space",
                        title = "Space (Balanced Storage)",
                        desc = "Compiles core initialization routines and entry points. Balances disk conservation with responsive app launch performance."
                    )
                    FilterItem(
                        mode = "verify",
                        title = "Verify (Bytecode Only)",
                        desc = "Executes DEX bytecode verification without generating native machine code. Clears AOT cache and frees maximum device storage space."
                    )
                    FilterItem(
                        mode = "everything",
                        title = "Everything (Exhaustive)",
                        desc = "Compiles all classes, methods, and auxiliary bytecode unconditionally. Guarantees 100% native execution across the entire application."
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterItem(
    mode: String,
    title: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerSmall),
        color = Color(0xFF090A0C),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = mode,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = DexorCodeFont),
                        color = Color(0xFF90CAF9),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF90A4AE),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ArchitectureCard() {
    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            MinimalInfoRow(label = "R8 ProGuard Shrink", value = "3.29 MB Release APK")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Search & Sort Pipeline", value = "Dispatchers.Default Offload")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Icon Disk Cache", value = "Lossy WebP 85%")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Dexopt Parser", value = "Zero-Allocation Single Pass")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "App Name Index", value = "Room v4 In-Memory Cache")
        }
    }
}

@Composable
private fun PrivacySafetyCard() {
    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "100% Offline & Private",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Text(
                text = "Dexor requests zero Internet permissions (android.permission.INTERNET is absent from the manifest). The application contains zero telemetry, analytics, tracking, or crash loggers. All operations remain entirely on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF90A4AE),
                lineHeight = 20.sp
            )

            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            Text(
                text = "System Safety Assurance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "All compilation operations execute standard Android platform commands ('cmd package compile') via Shizuku. Dexor does not alter read-only system partitions or modify device security configurations.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF90A4AE),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun CreditsLegalCard(
    showLicense: Boolean,
    onToggleLicense: () -> Unit,
    onOpenRepo: () -> Unit,
    onOpenReleases: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        color = Color(0xFF131720),
        border = BorderStroke(1.dp, Color(0xFF1E2636)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DexorDimensions.spaceDefault),
            verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)
        ) {
            MinimalInfoRow(label = "Developer", value = "DeveshTone")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "Pair Engineering", value = "Google Gemini & Antigravity")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            MinimalInfoRow(label = "License", value = "MIT Open Source")
            Divider(color = Color(0xFF1A2130), thickness = 0.8.dp)

            Text(
                text = "Third-Party Libraries",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            MinimalInfoRow(label = "Shizuku API", value = "Apache 2.0 (RikkaApps)")
            MinimalInfoRow(label = "Jetpack Compose", value = "Apache 2.0 (Google LLC)")
            MinimalInfoRow(label = "Room SQLite", value = "Apache 2.0 (Google LLC)")
            MinimalInfoRow(label = "Coil", value = "Apache 2.0 (Coil Contributors)")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DexorDimensions.spaceSmall)
            ) {
                OutlinedButton(
                    onClick = onOpenRepo,
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    border = BorderStroke(1.dp, Color(0xFF1E2636)),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = DexorDimensions.minTouchTarget),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
                ) {
                    Text("GitHub Repo", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onOpenReleases,
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    border = BorderStroke(1.dp, Color(0xFF1E2636)),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = DexorDimensions.minTouchTarget),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9))
                ) {
                    Text("Releases", style = MaterialTheme.typography.labelMedium)
                }
            }

            TextButton(
                onClick = onToggleLicense,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (showLicense) "Hide MIT License" else "View MIT License",
                    color = Color(0xFF90CAF9),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            AnimatedVisibility(visible = showLicense) {
                Surface(
                    shape = RoundedCornerShape(DexorDimensions.cornerSmall),
                    color = Color(0xFF090A0C),
                    border = BorderStroke(1.dp, Color(0xFF1E2636)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = """
                            MIT License
                            Copyright (c) 2026 DeveshTone

                            Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

                            The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

                            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = DexorCodeFont,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        ),
                        color = Color(0xFF90A4AE),
                        modifier = Modifier.padding(DexorDimensions.spaceMedium)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceSmall)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF90CAF9),
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun MinimalInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF90A4AE),
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFECEFF1),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}