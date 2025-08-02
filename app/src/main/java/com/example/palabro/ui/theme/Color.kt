package com.example.palabro.ui.theme

import androidx.compose.ui.graphics.Color

// --- Colores para las casillas del tablero ---
data class GameColors(
    val correct: Color,
    val wrongPosition: Color,
    val incorrect: Color
)

// --- Colores para el TEXTO de las teclas ---
data class KeyboardColors(
    val correct: Color,
    val wrongPosition: Color
)

// --- TEMA CLARO ---
val LightPrimary = Color(0xFF4CAF50)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF1F1F1)
val LightOnPrimary = Color.White
val LightOnBackground = Color.Black
val LightOnSurface = Color.Black
val LightGameColors = GameColors(
    correct = Color(0xFF6AAA64),
    wrongPosition = Color(0xFFC9B458),
    incorrect = Color(0xFF787C7E)
)
val LightKeyboardColors = KeyboardColors(
    correct = Color(0xFF6AAA64),
    wrongPosition = Color(0xFFC9B458)
)


// --- TEMA OSCURO ---
val DarkPrimary = Color(0xFF3DDC84)
val DarkBackground = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2C2C2C)
val DarkOnPrimary = Color.Black
val DarkOnBackground = Color.White
val DarkOnSurface = Color.White
val DarkGameColors = GameColors(
    correct = Color(0xFF538D4E),
    wrongPosition = Color(0xFFB59F3B),
    incorrect = Color(0xFF55595b)
)
val DarkKeyboardColors = KeyboardColors(
    correct = Color(0xFF538D4E),
    wrongPosition = Color(0xFFB59F3B)
)

// --- TEMA DRACULA ---
val DraculaPrimary = Color(0xFFBD93F9)      // Purple
val DraculaBackground = Color(0xFF282A36)  // Background
val DraculaSurface = Color(0xFF44475A)      // Current Line
val DraculaOnPrimary = Color(0xFFF8F8F2)    // Foreground
val DraculaOnBackground = Color(0xFFF8F8F2) // Foreground
val DraculaOnSurface = Color(0xFFF8F8F2)    // Foreground
val DraculaGameColors = GameColors(
    correct = Color(0xff44d368),       // Green
    wrongPosition = Color(0xffd1da62),   // Yellow
    incorrect = Color(0xFF6272A4)      // Comment
)
val DraculaKeyboardColors = KeyboardColors(
    correct = Color(0xFF50FA7B),       // Green
    wrongPosition = Color(0xffd1da62)    // Yellow
)

// --- TEMA PASTEL (CORREGIDO) ---
val PastelPrimary = Color(0xFFe3a3da)
val PastelBackground = Color(0xFFd6eae6)
val PastelSurface = Color(0xFFe7e7bc)
val PastelOnPrimary = Color(0xFFFFFFFF)
val PastelOnBackground = Color(0xFF5D5C61)
val PastelOnSurface = Color(0xFF5D5C61)
val PastelGameColors = GameColors(
    correct = Color(0xFF81c784),
    wrongPosition = Color(0xffeddb92),
    incorrect = Color(0xffed99a7)
)
val PastelKeyboardColors = KeyboardColors(
    correct = Color(0xFF558B2F),       // Verde oscuro para contraste
    wrongPosition = Color(0xFFF9A825)      // Amarillo oscuro para contraste
)

// --- TEMA SOLARIZED DARK ---
val SolarizedPrimary = Color(0xFF2aa198)
val SolarizedBackground = Color(0xFF002b36)
val SolarizedSurface = Color(0xFF073642)
val SolarizedOnPrimary = Color(0xFFfdf6e3)
val SolarizedOnBackground = Color(0xFF839496)
val SolarizedOnSurface = Color(0xFF93a1a1)
val SolarizedGameColors = GameColors(
    correct = Color(0xFF859900),
    wrongPosition = Color(0xFFb58900),
    incorrect = Color(0xFF586e75)
)
val SolarizedKeyboardColors = KeyboardColors(
    correct = Color(0xFF859900),
    wrongPosition = Color(0xFFb58900)
)


// --- TEMA RETRO ---
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
val RetroKeyboardColors = KeyboardColors(
    correct = Color(0xFF6B8E23),
    wrongPosition = Color(0xFFDAA520)
)