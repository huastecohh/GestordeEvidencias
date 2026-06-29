package com.example.gestordeevidencias.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightBluePrimary,
    onPrimary = Color.White,
    primaryContainer = LightBlueSecondary,
    onPrimaryContainer = LightBlueDark,

    secondary = AcademicGray,
    onSecondary = Color.White,
    secondaryContainer = AcademicGrayLight,
    onSecondaryContainer = AcademicGrayDark,

    tertiary = LightBlueSecondary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3E2),
    onTertiaryContainer = Color(0xFFA85C00),

    error = ErrorRed,
    errorContainer = Color(0xFFFCEEF2),

    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFECEEF8),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFFCBCEDF),
    outlineVariant = Color(0x1F03A9F4),

    inversePrimary = LightBlue80,
)

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue80,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF2A3670),
    onPrimaryContainer = LightBlueSecondary,

    secondary = AcademicGray80,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D3D2E),
    onSecondaryContainer = Color(0xFFE6F4F0),

    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF232638),
    onSurfaceVariant = Color(0xFFB0B5D4),
    outline = Color(0x33FFFFFF),
)

@Composable
fun GestorDeEvidenciasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}
