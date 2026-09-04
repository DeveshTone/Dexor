package com.wrick.dexor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrick.dexor.model.ShizukuState
import com.wrick.dexor.ui.theme.DexorDimensions

@Composable
fun PermissionsDialog(
    shizukuState: ShizukuState,
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Required Permissions",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DexorDimensions.spaceMedium)) {
                Text(
                    "Dexor requires elevated shell access via Shizuku to compile Android bytecode and inspect DEX optimization states without root.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0BEC5)
                )

                // Required Permission 1: Shizuku Service Running
                PermissionStatusItem(
                    title = "Shizuku Service Status",
                    subtitle = when (shizukuState) {
                        ShizukuState.READY, ShizukuState.NO_PERMISSION -> "Shizuku service is running"
                        ShizukuState.NOT_RUNNING -> "Service not running. Start Shizuku app via Wireless Debugging or Root."
                    },
                    isGranted = shizukuState != ShizukuState.NOT_RUNNING
                )

                // Required Permission 2: Shizuku App Authorization
                PermissionStatusItem(
                    title = "Shizuku Shell Authorization",
                    subtitle = when (shizukuState) {
                        ShizukuState.READY -> "Authorized for background DEX2OAT compilation"
                        ShizukuState.NO_PERMISSION -> "Authorization required. Tap 'Request Permission' below."
                        ShizukuState.NOT_RUNNING -> "Waiting for Shizuku service to be started"
                    },
                    isGranted = shizukuState == ShizukuState.READY
                )
            }
        },
        confirmButton = {
            if (shizukuState == ShizukuState.NO_PERMISSION) {
                Button(
                    onClick = {
                        onRequestPermission()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                ) {
                    Text("Request Permission", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                ) {
                    Text("Close", color = Color(0xFF90CAF9), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        dismissButton = {
            if (shizukuState == ShizukuState.NO_PERMISSION) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
                ) {
                    Text("Dismiss", color = Color(0xFF90A4AE), style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        containerColor = Color(0xFF131720),
        shape = RoundedCornerShape(DexorDimensions.cornerLarge)
    )
}

@Composable
private fun PermissionStatusItem(
    title: String,
    subtitle: String,
    isGranted: Boolean
) {
    Surface(
        color = if (isGranted) Color(0xFF1B2A1E) else Color(0xFF2C1618),
        shape = RoundedCornerShape(DexorDimensions.cornerMedium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(DexorDimensions.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFEF5350),
                modifier = Modifier.size(DexorDimensions.iconSizeMedium)
            )
            Spacer(Modifier.width(DexorDimensions.spaceMedium))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                )
            }
        }
    }
}

