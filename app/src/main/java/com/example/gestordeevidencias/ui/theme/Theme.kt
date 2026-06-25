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
    primary          = Indigo600,
    onPrimary        = Color.White,
    primaryContainer = Indigo50,
    onPrimaryContainer = Indigo900,

    secondary        = Teal600,
    onSecondary      = Color.White,
    secondaryContainer = Teal50,
    onSecondaryContainer = Teal700,

    tertiary         = Amber600,
    onTertiary       = Color.White,
    tertiaryContainer = Amber50,
    onTertiaryContainer = Amber700,

    error            = Rose600,
    errorContainer   = Rose50,

    background       = Surface1,
    onBackground     = Ink900,
    surface          = Surface0,
    onSurface        = Ink900,
    surfaceVariant   = Surface2,
    onSurfaceVariant = Ink700,
    outline          = Ink200,
    outlineVariant   = Color(0x1F3D52A0),  // Indigo600 @ 12% alpha

    inversePrimary   = Indigo400,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Indigo400,
    onPrimary        = Indigo900,
    primaryContainer = Color(0xFF2A3670),
    onPrimaryContainer = Indigo100,

    secondary        = Teal600,
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFF0D3D2E),
    onSecondaryContainer = Teal50,

    background       = DarkSurface1,
    onBackground     = DarkInk900,
    surface          = DarkSurface0,
    onSurface        = DarkInk900,
    surfaceVariant   = DarkSurface2,
    onSurfaceVariant = DarkInk700,
    outline          = Color(0x33FFFFFF),
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
