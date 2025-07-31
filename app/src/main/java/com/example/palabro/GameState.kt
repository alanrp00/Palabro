package com.example.palabro

// Fichero central para las definiciones de estado del juego.

data class Guess(
    val word: String,
    val statuses: List<LetterStatus>?,
    val isRevealed: Boolean = false // <-- CAMBIO REALIZADO
)

enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

enum class LetterStatus {
    CORRECT,
    WRONG_POSITION,
    INCORRECT
}