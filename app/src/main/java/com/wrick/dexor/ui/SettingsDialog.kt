package com.wrick.dexor.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wrick.dexor.model.ShizukuState
import com.wrick.dexor.ui.theme.DexorCodeFont

@Composable
fun SettingsDialog(
    shizukuState: ShizukuState,
    onRequestPermission: () -> Unit,
    onRefreshShizuku: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showLicenseText by remember { mutableStateOf(false) }
    var showModesGuide by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0D1117),
            tonalElevation = 16.dp,
            border = BorderStroke(1.dp, Color(0xFF21262D))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF161B22))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF388BFD).copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color(0xFF58A6FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Settings & About",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF0F6FC)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF21262D), thickness = 1.dp)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // HERO BRANDING CARD
                    HeroBrandCard()

                    // SECTION 1: Privilege & Service
                    SettingsSection(title = "PRIVILEGE & SYSTEM ACCESS") {
                        ShizukuStatusCard(
                            shizukuState = shizukuState,
                            onRequestPermission = onRequestPermission,
                            onRefreshShizuku = onRefreshShizuku
                        )
                    }

                    // SECTION 2: Device & Runtime Environment
                    SettingsSection(title = "DEVICE & RUNTIME ENVIRONMENT") {
                        DeviceEnvironmentCard()
                    }

                    // SECTION 3: Compilation Modes Cheatsheet
                    SettingsSection(title = "COMPILATION MODES GUIDE") {
                        CompilationModesCard(
                            isExpanded = showModesGuide,
                            onToggle = { showModesGuide = !showModesGuide }
                        )
                    }

                    // SECTION 4: Architecture & Optimizations
                    SettingsSection(title = "PERFORMANCE & ARCHITECTURE") {
                        PerformanceArchCard()
                    }

                    // SECTION 5: Privacy & Security
                    SettingsSection(title = "PRIVACY & SYSTEM INTEGRITY") {
                        PrivacySecurityCard()
                    }

                    // SECTION 6: Open Source & Legal
                    SettingsSection(title = "OPEN SOURCE & CREDITS") {
                        LegalCreditsCard(
                            showLicenseText = showLicenseText,
                            onToggleLicense = { showLicenseText = !showLicenseText },
                            onOpenGitHub = {
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

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroBrandCard() {
    val context = LocalContext.current
    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(width = 128, height = 128).asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.5.dp, Color(0xFF388BFD).copy(alpha = 0.5f)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (appIconBitmap != null) {
                            Image(
                                bitmap = appIconBitmap,
                                contentDescription = "Dexor Icon",
                                modifier = Modifier.size(46.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Dexor Icon",
                                tint = Color(0xFF58A6FF),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DEXOR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFFF0F6FC)
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF238636).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF238636).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "v1.0.0",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3FB950),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Unified DEX2OAT Runtime Manager",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B949E),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickBadge(label = "Non-Root ART", color = Color(0xFF58A6FF))
                QuickBadge(label = "R8 Shrink (3.29MB)", color = Color(0xFFBC8CFF))
                QuickBadge(label = "100% Offline", color = Color(0xFF3FB950))
            }
        }
    }
}

@Composable
private fun QuickBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontSize = 11.sp,
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
    val statusColor = when (shizukuState) {
        ShizukuState.READY -> Color(0xFF3FB950)
        ShizukuState.NO_PERMISSION -> Color(0xFFD29922)
        ShizukuState.NOT_RUNNING -> Color(0xFFF85149)
    }
    val statusLabel = when (shizukuState) {
        ShizukuState.READY -> "Authorized & Connected"
        ShizukuState.NO_PERMISSION -> "Authorization Pending"
        ShizukuState.NOT_RUNNING -> "Service Inactive"
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Shizuku Service",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFF0F6FC),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = when (shizukuState) {
                    ShizukuState.READY -> "Dexor is communicating with Shizuku via elevated IPC. Full native cmd package compile execution is operational."
                    ShizukuState.NO_PERMISSION -> "Shizuku server is active, but Dexor requires one-time authorization. Tap below to grant permission."
                    ShizukuState.NOT_RUNNING -> "Shizuku daemon is not running. Please start Shizuku via Wireless Debugging (ADB) or root."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                lineHeight = 18.sp
            )

            if (shizukuState == ShizukuState.NO_PERMISSION) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6FEB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Authorize Dexor with Shizuku", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            } else if (shizukuState == ShizukuState.NOT_RUNNING) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRefreshShizuku,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF388BFD)),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF58A6FF))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Re-probe Shizuku Service")
                }
            }
        }
    }
}

@Composable
private fun DeviceEnvironmentCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
            val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"

            SettingsInfoRow(label = "Hardware Model", value = deviceModel)
            SettingsInfoRow(label = "Operating System", value = osVersion)
            SettingsInfoRow(label = "CPU Architecture", value = primaryAbi)
            SettingsInfoRow(label = "Runtime Engine", value = "ART (DEX2OAT)")
            SettingsInfoRow(label = "Compilation Backend", value = "cmd package compile")
        }
    }
}

@Composable
private fun CompilationModesCard(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ART Filter Reference",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFF0F6FC),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Supported compiler modes explained",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B949E),
                        fontSize = 11.sp
                    )
                }

                TextButton(onClick = onToggle) {
                    Text(
                        text = if (isExpanded) "Collapse" else "Learn More",
                        color = Color(0xFF58A6FF),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeExplainerItem(
                        filter = "speed",
                        title = "Full Ahead-Of-Time (Speed)",
                        desc = "Compiles application bytecode into native machine instructions ahead of time. Maximizes runtime execution velocity and eliminates JIT compilation lag."
                    )
                    ModeExplainerItem(
                        filter = "space",
                        title = "Storage-Optimized (Space)",
                        desc = "Compiles core initialization routines and entry points. Minimizes disk storage footprint while providing solid app launch responsiveness."
                    )
                    ModeExplainerItem(
                        filter = "verify",
                        title = "Bytecode Verification Only (Verify)",
                        desc = "Executes DEX bytecode verification without generating native machine code. Clears AOT cache and frees maximum device storage space."
                    )
                    ModeExplainerItem(
                        filter = "everything",
                        title = "Exhaustive Compilation (Everything)",
                        desc = "Compiles all classes, methods, and auxiliary bytecode unconditionally. Guarantees 100% native execution but consumes the largest storage volume."
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeExplainerItem(
    filter: String,
    title: String,
    desc: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF21262D), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF0F6FC)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF58A6FF).copy(alpha = 0.15f)
            ) {
                Text(
                    text = filter,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = DexorCodeFont),
                    color = Color(0xFF79C0FF),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 11.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B949E),
            fontSize = 11.5.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PerformanceArchCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Engineered for Zero-Jank Efficiency",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFF0F6FC),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Dexor is built strictly following modern Android performance practices:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                fontSize = 12.sp
            )

            SettingsInfoRow(label = "Minification & Stripping", value = "R8 Full ProGuard (3.29 MB)")
            SettingsInfoRow(label = "Search & Filter Execution", value = "Dispatchers.Default Offload")
            SettingsInfoRow(label = "Package State Parser", value = "Zero-Allocation Dumpsys")
            SettingsInfoRow(label = "Icon Pipeline", value = "Lossy WebP 85% Disk Cache")
            SettingsInfoRow(label = "Database Engine", value = "Room v4 Indexed Cache")
        }
    }
}

@Composable
private fun PrivacySecurityCard() {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF238636).copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF3FB950),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "100% Offline • Zero Network Access",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFF0F6FC),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "The android.permission.INTERNET declaration is completely absent from Dexor's AndroidManifest. The app has zero capability to contact remote servers, upload analytics, or transmit your device data. All operations happen strictly on-device.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                lineHeight = 17.sp,
                fontSize = 12.sp
            )

            Divider(color = Color(0xFF21262D), thickness = 1.dp)

            Text(
                text = "System Safety Assurance",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFF0F6FC),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Compilation calls execute standard Android platform utilities ('cmd package compile') via Shizuku. Dexor does not mount read-write system partitions, modify kernel settings, or void system integrity.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                lineHeight = 17.sp,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LegalCreditsCard(
    showLicenseText: Boolean,
    onToggleLicense: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenReleases: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = BorderStroke(1.dp, Color(0xFF30363D)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingsInfoRow(label = "Lead Developer", value = "DeveshTone")
            SettingsInfoRow(label = "AI Pair Engineering", value = "Google Gemini & Antigravity")
            SettingsInfoRow(label = "License", value = "MIT Open Source License")

            Divider(color = Color(0xFF21262D), thickness = 1.dp)

            Text(
                text = "Third-Party Acknowledgements",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF0F6FC)
            )

            LicenseItem(name = "Shizuku API", license = "Apache-2.0 © RikkaApps")
            LicenseItem(name = "Android Jetpack & Compose", license = "Apache-2.0 © Google LLC")
            LicenseItem(name = "Room SQLite Persistence", license = "Apache-2.0 © Google LLC")
            LicenseItem(name = "Coil Image Loader", license = "Apache-2.0 © Coil Contributors")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenGitHub,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF30363D)),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF58A6FF))
                ) {
                    Text("GitHub Repo", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenReleases,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF30363D)),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF58A6FF))
                ) {
                    Text("Releases", fontSize = 12.sp)
                }
            }

            TextButton(
                onClick = onToggleLicense,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = if (showLicenseText) "Hide License" else "View Full MIT License",
                    color = Color(0xFF58A6FF),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            AnimatedVisibility(visible = showLicenseText) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.dp, Color(0xFF21262D)),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
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
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp
                        ),
                        color = Color(0xFF8B949E),
                        modifier = Modifier.padding(12.dp)
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF58A6FF),
            letterSpacing = 1.1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        content()
    }
}

@Composable
private fun SettingsInfoRow(
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
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B949E),
            fontSize = 12.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFF0F6FC),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LicenseItem(
    name: String,
    license: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFF0F6FC),
            fontSize = 12.sp
        )
        Text(
            text = license,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B949E),
            fontSize = 11.5.sp
        )
    }
}