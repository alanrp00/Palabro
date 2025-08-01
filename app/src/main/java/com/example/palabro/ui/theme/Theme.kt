package com.example.palabro.ui.theme

import android.app.Activity
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeStyle {
    SYSTEM, LIGHT, DARK, DRACULA, Solarized, PASTEL, RETRO
}

// Creamos un "proveedor" de CompositionLocal para nuestros colores de juego
val LocalGameColors = staticCompositionLocalOf { LightGameColors }

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
)

private val DraculaColorScheme = darkColorScheme(
    primary = DraculaPrimary,
    background = DraculaBackground,
    surface = DraculaSurface,
    onPrimary = DraculaOnPrimary,
    onBackground = DraculaOnBackground,
    onSurface = DraculaOnSurface,
)

private val SolarizedColorScheme = darkColorScheme(
    primary = SolarizedPrimary,
    background = SolarizedBackground,
    surface = SolarizedSurface,
    onPrimary = SolarizedOnPrimary,
    onBackground = SolarizedOnBackground,
    onSurface = SolarizedOnSurface,
)

private val PastelColorScheme = lightColorScheme(
    primary = PastelPrimary,
    background = PastelBackground,
    surface = PastelSurface,
    onPrimary = PastelOnPrimary,
    onBackground = PastelOnBackground,
    onSurface = PastelOnSurface,
)

private val RetroColorScheme = lightColorScheme(
    primary = RetroPrimary,
    background = RetroBackground,
    surface = RetroSurface,
    onPrimary = RetroOnPrimary,
    onBackground = RetroOnBackground,
    onSurface = RetroOnSurface,
)


@Composable
fun PalabroTheme(
    themeStyle: ThemeStyle = ThemeStyle.SYSTEM,
    content: @Composable () -> Unit
) {
    Log.d("PalabroThemeCheck", "Aplicando el tema: $themeStyle")

    val isDark = when (themeStyle) {
        ThemeStyle.LIGHT -> false
        ThemeStyle.PASTEL -> false
        ThemeStyle.RETRO -> false
        ThemeStyle.DARK -> true
        ThemeStyle.DRACULA -> true
        ThemeStyle.Solarized -> true
        ThemeStyle.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (themeStyle) {
        ThemeStyle.DARK -> DarkColorScheme
        ThemeStyle.DRACULA -> DraculaColorScheme
        ThemeStyle.Solarized -> SolarizedColorScheme
        ThemeStyle.PASTEL -> PastelColorScheme
        ThemeStyle.RETRO -> RetroColorScheme
        else -> if (isDark) DarkColorScheme else LightColorScheme
    }

    val gameColors = when (themeStyle) {
        ThemeStyle.DARK -> DarkGameColors
        ThemeStyle.DRACULA -> DraculaGameColors
        ThemeStyle.Solarized -> SolarizedGameColors
        ThemeStyle.PASTEL -> PastelGameColors
        ThemeStyle.RETRO -> RetroGameColors
        else -> if (isDark) DarkGameColors else LightGameColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    CompositionLocalProvider(LocalGameColors provides gameColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}