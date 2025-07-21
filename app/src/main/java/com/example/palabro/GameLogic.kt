package com.example.palabro

import kotlin.random.Random

// Define los tres estados posibles para cada letra de un intento.
enum class LetterStatus {
    CORRECT,          // Letra correcta en la posición correcta (verde)
    WRONG_POSITION,   // Letra correcta en la posición incorrecta (amarillo)
    INCORRECT         // La letra no está en la palabra (gris)
}

// Esta clase será el "molde" para nuestro motor de juego.
// Contendrá toda la lógica para que el juego funcione.
class GameLogic {

    // 1. EL DICCIONARIO
    private val wordList = listOf("CASA", "GATO", "PERRO", "MESA", "SOLAR", "JUEGO", "GOTAS")

    // 2. ESTADO DEL JUEGO
    var secretWord: String = ""
        private set

    val maxAttempts = 6
    val guesses = mutableListOf<String>()

    // 3. INICIO DEL JUEGO
    init {
        startNewGame()
    }

    fun startNewGame() {
        secretWord = wordList.random(Random(System.currentTimeMillis()))
        guesses.clear()
        // Para pruebas, podemos imprimir la palabra secreta en la consola de Android Studio
        println("La palabra secreta es: $secretWord")
    }

    // 4. LÓGICA PRINCIPAL (¡AHORA COMPLETA!)
    // Esta función ahora devuelve una lista con el estado de cada letra.
    fun submitGuess(guess: String): List<LetterStatus> {
        val upperGuess = guess.uppercase()
        guesses.add(upperGuess)

        val result = MutableList(secretWord.length) { LetterStatus.INCORRECT }
        val secretWordLetterCounts = secretWord.groupingBy { it }.eachCount().toMutableMap()

        // --- PASO 1: Buscar aciertos directos (CORRECT - verdes) ---
        // Primero recorremos la palabra para encontrar las letras que están en la posición correcta.
        // Esto es importante para gestionar letras duplicadas correctamente.
        upperGuess.forEachIndexed { index, char ->
            if (index < secretWord.length && char == secretWord[index]) {
                result[index] = LetterStatus.CORRECT
                // Decrementamos el contador de esta letra para no volver a usarla.
                secretWordLetterCounts[char] = secretWordLetterCounts.getOrDefault(char, 0) - 1
            }
        }

        // --- PASO 2: Buscar letras en posición incorrecta (WRONG_POSITION - amarillas) ---
        // Volvemos a recorrer la palabra para encontrar las letras que sí existen, pero en otro sitio.
        upperGuess.forEachIndexed { index, char ->
            // Solo procesamos las que no fueron aciertos directos en el paso 1.
            if (result[index] == LetterStatus.INCORRECT) {
                if (secretWordLetterCounts.getOrDefault(char, 0) > 0) {
                    result[index] = LetterStatus.WRONG_POSITION
                    // Decrementamos el contador para no marcarla de nuevo si aparece otra vez.
                    secretWordLetterCounts[char] = secretWordLetterCounts.getOrDefault(char, 0) - 1
                }
            }
        }

        return result
    }
}