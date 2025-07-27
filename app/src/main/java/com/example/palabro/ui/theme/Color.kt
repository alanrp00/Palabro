package com.example.palabro.ui.theme

import androidx.compose.ui.graphics.Color

// Estructura para los colores específicos del juego
data class GameColors(
    val correct: Color,
    val wrongPosition: Color,
    val incorrect: Color
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
    incorrect = Color(0xFF3A3A3C)
)

// --- TEMA DRACULA (ACTUALIZADO) ---
val DraculaPrimary = Color(0xFFBD93F9) // Purple
val DraculaBackground = Color(0xFF282A36) // Background
val DraculaSurface = Color(0xFF44475A) // Current Line
val DraculaOnPrimary = Color(0xFFF8F8F2) // Foreground
val DraculaOnBackground = Color(0xFFF8F8F2) // Foreground
val DraculaOnSurface = Color(0xFFF8F8F2) // Foreground
val DraculaGameColors = GameColors(
    correct = Color(0xFF50FA7B), // Green
    wrongPosition = Color(0xFFE1E986), // Yellow
    incorrect = Color(0xFF6272A4) // Comment
)

// --- TEMA HACKER / TERMINAL ---
val HackerPrimary = Color(0xFF39FF14)
val HackerBackground = Color(0xFF000000)
val HackerSurface = Color(0xFF1C1C1C)
val HackerOnPrimary = Color.Black
val HackerOnBackground = Color(0xFF39FF14)
val HackerOnSurface = Color(0xFF39FF14)
val HackerGameColors = GameColors(
    correct = Color(0xFF39FF14),
    wrongPosition = Color(0xFFb8860b),
    incorrect = Color(0xFF333333)
)

// --- TEMA PASTEL ---
val PastelPrimary = Color(0xFFe3a3da) // color1
val PastelBackground = Color(0xFFd6eae6) // color5
val PastelSurface = Color(0xFFe7e7bc) // color4
val PastelOnPrimary = Color(0xFFFFFFFF)
val PastelOnBackground = Color(0xFF5D5C61) // Un gris oscuro para el texto
val PastelOnSurface = Color(0xFF5D5C61) // Un gris oscuro para el texto
val PastelGameColors = GameColors(
    correct = Color(0xFF82E182), // Un verde pastel que combina
    wrongPosition = Color(0xFFdfcaa2), // color3
    incorrect = Color(0xFFe2abb3) // color2
)

// --- NUEVO TEMA: RETRO / VINTAGE ---
val RetroPrimary = Color(0xFF008080) // Teal
val RetroBackground = Color(0xFFF5EFE6) // Crema / Papel antiguo
val RetroSurface = Color(0xFFE8DFD1) // Beige claro
val RetroOnPrimary = Color.White
val RetroOnBackground = Color(0xFF4E463F) // Marrón oscuro para texto
val RetroOnSurface = Color(0xFF4E463F) // Marrón oscuro para texto
val RetroGameColors = GameColors(
    correct = Color(0xFF6B8E23), // Verde Oliva
    wrongPosition = Color(0xFFDAA520), // Amarillo Mostaza / Dorado
    incorrect = Color(0xFF8A7F7C) // Gris pardo
)