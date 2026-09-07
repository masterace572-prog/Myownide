package com.anoy.ide.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Forge design tokens.
 *
 * The palette is intentionally quiet: warm neutral surfaces, a single terracotta
 * accent, and muted state colors. No gradients, no neon, no colored pills.
 */
object ForgeColors {
    // Light surfaces
    val SurfaceBackgroundLight = Color(0xFFF5F4EF) // ivory
    val SurfaceElevatedLight = Color(0xFFFFFFFF)
    val SurfaceSubtleLight = Color(0xFFECEAE3)

    // Dark surfaces
    val SurfaceBackgroundDark = Color(0xFF1F1E1D) // charcoal
    val SurfaceElevatedDark = Color(0xFF2B2A27)
    val SurfaceSubtleDark = Color(0xFF33312E)

    // Text
    val TextPrimaryLight = Color(0xFF1F1E1D)
    val TextSecondaryLight = Color(0xFF6B6860)

    val TextPrimaryDark = Color(0xFFF5F4EF)
    val TextSecondaryDark = Color(0xFFA8A49B)

    // Accent (clay / terracotta)
    val AccentPrimaryLight = Color(0xFFC96442)
    val AccentPrimaryDark = Color(0xFFD97757)
    val AccentOnLight = Color(0xFFFFFFFF)
    val AccentOnDark = Color(0xFF1F1E1D)

    // State colors
    val ErrorLight = Color(0xFFB3261E)
    val ErrorDark = Color(0xFFE5776A)
    val SuccessLight = Color(0xFF4F7A5B)
    val SuccessDark = Color(0xFF86B096)
    val WarningLight = Color(0xFF9A6B1B)
    val WarningDark = Color(0xFFD2A24C)
}
