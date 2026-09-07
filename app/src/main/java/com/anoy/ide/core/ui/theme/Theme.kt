package com.anoy.ide.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ForgeColors.AccentPrimaryLight,
    onPrimary = ForgeColors.AccentOnLight,
    secondary = ForgeColors.AccentPrimaryLight,
    onSecondary = ForgeColors.AccentOnLight,
    tertiary = ForgeColors.AccentPrimaryLight,
    onTertiary = ForgeColors.AccentOnLight,
    background = ForgeColors.SurfaceBackgroundLight,
    onBackground = ForgeColors.TextPrimaryLight,
    surface = ForgeColors.SurfaceElevatedLight,
    onSurface = ForgeColors.TextPrimaryLight,
    surfaceVariant = ForgeColors.SurfaceSubtleLight,
    onSurfaceVariant = ForgeColors.TextSecondaryLight,
    error = ForgeColors.ErrorLight,
    onError = ForgeColors.AccentOnLight,
    outline = ForgeColors.TextSecondaryLight.copy(alpha = 0.6f),
    outlineVariant = ForgeColors.SurfaceSubtleLight
)

private val DarkColorScheme = darkColorScheme(
    primary = ForgeColors.AccentPrimaryDark,
    onPrimary = ForgeColors.AccentOnDark,
    secondary = ForgeColors.AccentPrimaryDark,
    onSecondary = ForgeColors.AccentOnDark,
    tertiary = ForgeColors.AccentPrimaryDark,
    onTertiary = ForgeColors.AccentOnDark,
    background = ForgeColors.SurfaceBackgroundDark,
    onBackground = ForgeColors.TextPrimaryDark,
    surface = ForgeColors.SurfaceElevatedDark,
    onSurface = ForgeColors.TextPrimaryDark,
    surfaceVariant = ForgeColors.SurfaceSubtleDark,
    onSurfaceVariant = ForgeColors.TextSecondaryDark,
    error = ForgeColors.ErrorDark,
    onError = ForgeColors.AccentOnDark,
    outline = ForgeColors.TextSecondaryDark.copy(alpha = 0.6f),
    outlineVariant = ForgeColors.SurfaceSubtleDark
)

@Composable
fun ForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ForgeType.Typography,
        shapes = ForgeShapes.Shapes,
        content = content
    )
}
