package com.example.palabro.ui.theme

import androidx.compose.ui.graphics.Color

// Estructura para los colores específicos del juego
data class GameColors(
    val correct: Color,
    val wrongPosition: Color,
    val incorrect: Color
)

// --- TEMA CLARO (AJUSTADO) ---
val LightPrimary = Color(0xFF4CAF50)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF1F1F1)
val LightOnPrimary = Color.White
val LightOnBackground = Color.Black
val LightOnSurface = Color.Black
val LightGameColors = GameColors(
    correct = Color(0xFF6AAA64),
    wrongPosition = Color(0xFFB59F3B), // Amarillo más oscuro para mejor contraste
    incorrect = Color(0xFF787C7E)   // Gris sin cambios, ya era bastante oscuro
)

// --- TEMA OSCURO (AJUSTADO) ---
val DarkPrimary = Color(0xFF3DDC84)
val DarkBackground = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2C2C2C)
val DarkOnPrimary = Color.Black
val DarkOnBackground = Color.White
val DarkOnSurface = Color.White
val DarkGameColors = GameColors(
    correct = Color(0xFF538D4E),
    wrongPosition = Color(0xFFB59F3B), // Amarillo sin cambios, buen contraste
    incorrect = Color(0xFF55595b)   // Gris un poco más claro para distinguirse mejor
)

// --- TEMA DRACULA (AJUSTADO) ---
val DraculaPrimary = Color(0xFFBD93F9)
val DraculaBackground = Color(0xFF282A36)
val DraculaSurface = Color(0xFF44475A)
val DraculaOnPrimary = Color(0xFFF8F8F2)
val DraculaOnBackground = Color(0xFFF8F8F2)
val DraculaOnSurface = Color(0xFFF8F8F2)
val DraculaGameColors = GameColors(
    correct = Color(0xFF47c269),     // Verde menos brillante
    wrongPosition = Color(0xFFd4d676), // Amarillo menos brillante
    incorrect = Color(0xFF58658f)    // Azul un poco más apagado
)

// --- TEMA PASTEL (AJUSTADO) ---
val PastelPrimary = Color(0xFFe3a3da)
val PastelBackground = Color(0xFFd6eae6)
val PastelSurface = Color(0xFFe7e7bc)
val PastelOnPrimary = Color(0xFFFFFFFF)
val PastelOnBackground = Color(0xFF5D5C61)
val PastelOnSurface = Color(0xFF5D5C61)
val PastelGameColors = GameColors(
    correct = Color(0xFF81c784),     // Verde menos brillante y más pastel
    wrongPosition = Color(0xFFdfcaa2),
    incorrect = Color(0xFFe2abb3)
)

// --- NUEVO TEMA: SOLARIZED DARK (Reemplaza a Hacker) ---
// Una paleta de alto contraste muy famosa y probada.
val SolarizedPrimary = Color(0xFF2aa198) // cyan
val SolarizedBackground = Color(0xFF002b36) // base03
val SolarizedSurface = Color(0xFF073642) // base02
val SolarizedOnPrimary = Color(0xFFfdf6e3) // base3
val SolarizedOnBackground = Color(0xFF839496) // base0
val SolarizedOnSurface = Color(0xFF93a1a1) // base1
val SolarizedGameColors = GameColors(
    correct = Color(0xFF859900),       // green
    wrongPosition = Color(0xFFb58900), // yellow
    incorrect = Color(0xFF586e75)      // base01 (gris oscuro)
)


// --- TEMA RETRO (SIN CAMBIOS) ---
val RetroPrimary = Color(0xFF008080)
val RetroBackground = Color(0xFFF5EFE6)
val RetroSurface = Color(0xFFE8DFD1)
val RetroOnPrimary = Color.White
val RetroOnBackground = Color(0xFF4E463F)
val RetroOnSurface = Color(0xFF4E463F)
val RetroGameColors = GameColors(
    correct = Color(0xFF6B8E23),
    wrongPosition = Color(0xFFDAA520),
    incorrect = Color(0xFF8A7F7C)
)