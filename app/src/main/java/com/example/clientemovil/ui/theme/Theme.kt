package com.example.clientemovil.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 🌙 Esquema oscuro
private val DarkColorScheme = darkColorScheme(
    primary   = BrownBorder,
    secondary = GrayHover,
    tertiary  = LightGrayBg,
    background = BlackText,
    surface    = BrownBorder,
    onPrimary  = WhiteCard,
    onSecondary = BlackText,
    onBackground = WhiteCard,
    onSurface = WhiteCard
)

// ☀️ Esquema claro
private val LightColorScheme = lightColorScheme(
    primary   = BrownBorder,     // Marrón tipo madera
    secondary = CreamBackground, // Fondo crema
    tertiary  = LightGrayBg,     // Gris muy claro
    background = CreamBackground,
    surface    = WhiteCard,      // Tarjetas blancas
    onPrimary  = WhiteCard,      // Texto encima del marrón
    onSecondary = BlackText,     // Texto encima del crema
    onBackground = BlackText,    // Texto principal
    onSurface = BlackText        // Texto sobre tarjetas
)

@Composable
fun ClienteMovilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
