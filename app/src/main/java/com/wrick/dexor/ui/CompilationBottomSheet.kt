package com.wrick.dexor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrick.dexor.model.COMPILE_MODES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompilationBottomSheet(
    appName: String?,
    batchCount: Int = 0,
    onDismissRequest: () -> Unit,
    onCompileModeSelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF131720)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (appName != null) "Compile: $appName"
                       else "Batch Optimization ($batchCount apps)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
            Divider(color = Color(0xFF1E2430))

            COMPILE_MODES.forEach { mode ->
                Surface(
                    color = Color.Transparent,
                    onClick = { onCompileModeSelected(mode.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF90A4AE)
                        )
                        if (batchCount > 0) {
                            val totalSec = mode.estimatedSecondsPerApp * batchCount
                            val minutes = totalSec / 60
                            val seconds = totalSec % 60
                            Text(
                                text = "Estimated total: ${if (minutes > 0) "${minutes}m " else ""}${seconds}s ($batchCount packages)",
                                fontSize = 11.sp,
                                color = Color(0xFF90CAF9),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

