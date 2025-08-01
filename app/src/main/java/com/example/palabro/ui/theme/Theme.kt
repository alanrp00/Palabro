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
    SYSTEM, LIGHT, DARK, DRACULA, SOLARIZED, PASTEL, RETRO
}

val LocalGameColors = staticCompositionLocalOf { LightGameColors }
val LocalKeyboardColors = staticCompositionLocalOf { LightKeyboardColors } // <-- NUEVO

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)
// ... (resto de esquemas de color no cambian)
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

private val SolarizedColorScheme = darkColorScheme(
    primary = SolarizedPrimary,
    background = SolarizedBackground,
    surface = SolarizedSurface,
    onPrimary = SolarizedOnPrimary,
    onBackground = SolarizedOnBackground,
    onSurface = SolarizedOnSurface,
)

@Composable
fun PalabroTheme(
    themeStyle: ThemeStyle = ThemeStyle.SYSTEM,
    content: @Composable () -> Unit
) {
    Log.d("PalabroThemeCheck", "Aplicando el tema: $themeStyle")

    val isDark = when (themeStyle) {
        ThemeStyle.LIGHT, ThemeStyle.PASTEL, ThemeStyle.RETRO -> false
        ThemeStyle.DARK, ThemeStyle.DRACULA, ThemeStyle.SOLARIZED -> true
        ThemeStyle.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (themeStyle) {
        ThemeStyle.DARK -> DarkColorScheme
        ThemeStyle.DRACULA -> DraculaColorScheme
        ThemeStyle.PASTEL -> PastelColorScheme
        ThemeStyle.RETRO -> RetroColorScheme
        ThemeStyle.SOLARIZED -> SolarizedColorScheme
        else -> if (isDark) DarkColorScheme else LightColorScheme
    }

    val gameColors = when (themeStyle) {
        ThemeStyle.DARK -> DarkGameColors
        ThemeStyle.DRACULA -> DraculaGameColors
        ThemeStyle.PASTEL -> PastelGameColors
        ThemeStyle.RETRO -> RetroGameColors
        ThemeStyle.SOLARIZED -> SolarizedGameColors
        else -> if (isDark) DarkGameColors else LightGameColors
    }

    // --- NUEVO: Configuración de colores de teclado ---
    val keyboardColors = when (themeStyle) {
        ThemeStyle.DARK -> DarkKeyboardColors
        ThemeStyle.DRACULA -> DraculaKeyboardColors
        ThemeStyle.PASTEL -> PastelKeyboardColors
        ThemeStyle.RETRO -> RetroKeyboardColors
        ThemeStyle.SOLARIZED -> SolarizedKeyboardColors
        else -> if (isDark) DarkKeyboardColors else LightKeyboardColors
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

    // Proveemos ambos grupos de colores a la app
    CompositionLocalProvider(
        LocalGameColors provides gameColors,
        LocalKeyboardColors provides keyboardColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}