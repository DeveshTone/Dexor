package com.wrick.dexor.ui

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrick.dexor.R
import com.wrick.dexor.model.AppInfo
import com.wrick.dexor.model.InstallSource
import com.wrick.dexor.ui.theme.DexorCodeFont
import com.wrick.dexor.ui.theme.DexorDimensions
import com.wrick.dexor.ui.util.AppIconImageLoader
import com.wrick.dexor.ui.util.AppIconKey
import coil.compose.AsyncImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppInfo,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val bg = if (isSelected) Color(0xFF1E293B) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = DexorDimensions.minTouchTarget)
            .background(bg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .drawBehind {
                drawLine(
                    color = Color(0xFF151921),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1f
                )
            }
            .padding(
                horizontal = DexorDimensions.spaceDefault,
                vertical = DexorDimensions.spaceSmall
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = DexorDimensions.spaceSmall)
            )
        }

        // Original Uncropped App Icon with soft rounded corners (LibChecker style via Coil)
        AsyncImage(
            model = remember(app.packageName) { AppIconKey(app.packageName) },
            contentDescription = null,
            imageLoader = AppIconImageLoader.get(context),
            modifier = Modifier
                .size(DexorDimensions.appIconSizeList)
                .clip(RoundedCornerShape(DexorDimensions.cornerSmall))
                .background(Color(0xFF1E2433))
        )

        Spacer(modifier = Modifier.width(DexorDimensions.spaceMedium))

        Column(modifier = Modifier.weight(1f)) {
            // Row 1: App name
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFECEFF1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 2: Package name with JetBrains Mono monospace font
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = DexorCodeFont),
                color = Color(0xFF90A4AE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(DexorDimensions.spaceSmall))

            // Row 3: Status badges with 48dp touch targets
            Row(
                horizontalArrangement = Arrangement.spacedBy(DexorDimensions.spaceSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = app.dexStatus, color = statusColor(app.dexStatus))
                StatusChip(text = app.dexReason, color = Color(0xFF263238))
                if (app.source == InstallSource.GOOGLE_PLAY) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1B2838), RoundedCornerShape(DexorDimensions.cornerSmall))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_play),
                            contentDescription = "Google Play",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else {
                    StatusChip(
                        text = sourceLabel(app.source),
                        color = sourceColor(app.source)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = Color.White,
        modifier = Modifier
            .background(color, RoundedCornerShape(DexorDimensions.cornerSmall))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

private fun statusColor(status: String): Color = when (status.lowercase()) {
    "speed", "speed-profile" -> Color(0xFF2E7D32)
    "everything" -> Color(0xFF1565C0)
    "verify" -> Color(0xFFE65100)
    "space" -> Color(0xFFF57F17)
    "run-from-apk", "interpret-only" -> Color(0xFFC62828)
    "n/a" -> Color(0xFF616161)
    else -> Color(0xFF455A64)
}

private fun sourceLabel(s: InstallSource) = when (s) {
    InstallSource.SYSTEM -> "SYS"
    InstallSource.GOOGLE_PLAY -> "PLAY"
    InstallSource.USER -> "USER"
}

private fun sourceColor(s: InstallSource) = when (s) {
    InstallSource.SYSTEM -> Color(0xFF455A64)
    InstallSource.GOOGLE_PLAY -> Color(0xFF1B5E20)
    InstallSource.USER -> Color(0xFF0D47A1)
}

