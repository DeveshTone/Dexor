package com.wrick.dexor.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 Spacing & Dimension Tokens
 * Single source of truth for all margins, paddings, and touch targets.
 */
object DexorDimensions {
    // Spacing
    val spaceNone: Dp = 0.dp
    val spaceExtraSmall: Dp = 4.dp
    val spaceSmall: Dp = 8.dp
    val spaceMedium: Dp = 12.dp
    val spaceDefault: Dp = 16.dp
    val spaceLarge: Dp = 24.dp
    val spaceExtraLarge: Dp = 32.dp

    // Touch Targets (All interactive elements meet WCAG / Material 3 48dp minimum)
    val minTouchTarget: Dp = 48.dp
    val iconSizeSmall: Dp = 20.dp
    val iconSizeMedium: Dp = 24.dp
    val appIconSizeList: Dp = 44.dp
    val appIconSizeDetail: Dp = 56.dp

    // Corner Radii
    val cornerSmall: Dp = 8.dp
    val cornerMedium: Dp = 12.dp
    val cornerLarge: Dp = 16.dp
    val cornerPill: Dp = 24.dp
}

