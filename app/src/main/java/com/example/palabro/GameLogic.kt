package com.example.palabro

import android.content.Context
import kotlin.random.Random
import android.util.Log

enum class LetterStatus {
    CORRECT,
    WRONG_POSITION,
    INCORRECT
}

// CAMBIO: Pequeña función para quitar acentos de las palabras.
private fun String.normalize(): String {
    return this.replace('Á', 'A')
        .replace('É', 'E')
        .replace('Í', 'I')
        .replace('Ó', 'O')
        .replace('Ú', 'U')
}

class GameLogic(private val context: Context, val wordLength: Int) {

    private val TAG = "GameLogic" // Etiqueta para nuestros mensajes
    private var wordList: List<String> = emptyList()
    // CAMBIO: Un conjunto de palabras normalizadas para una validación más rápida.
    private var normalizedWordSet: Set<String> = emptySet()

    var secretWord: String = ""
        private set

    val maxAttempts = 6
    val guesses = mutableListOf<String>()

    init {
        loadWords()
        startNewGame()
    }

    private fun loadWords() {
        val resourceId = when (wordLength) {
            5 -> R.raw.words_5_es
            6 -> R.raw.words_6_es
            7 -> R.raw.words_7_es
            else -> R.raw.words_5_es
        }

        context.resources.openRawResource(resourceId).bufferedReader().useLines { lines ->
            val words = lines.map { it.uppercase() }.toList()
            wordList = words
            // CAMBIO: Creamos la lista de palabras sin acentos para la validación.
            normalizedWordSet = words.map { it.normalize() }.toSet()
        }
    }

    // CAMBIO: Ahora comprueba contra la lista de palabras sin acentos.
    fun isValidWord(word: String): Boolean {
        // El 'word' del usuario no tendrá acentos, así que la comprobación es directa.
        return normalizedWordSet.contains(word.uppercase())
    }

    fun startNewGame() {
        if (wordList.isNotEmpty()) {
            secretWord = wordList.random(Random(System.currentTimeMillis()))
            guesses.clear()
            Log.d(TAG, "La palabra secreta ($wordLength letras) es: $secretWord (Normalizada: ${secretWord.normalize()})")
        }
    }

    fun submitGuess(guess: String): List<LetterStatus> {
        val upperGuess = guess.uppercase()
        guesses.add(upperGuess)

        // CAMBIO: Comparamos el intento del usuario (sin acentos) con la palabra secreta normalizada.
        val normalizedSecretWord = secretWord.normalize()

        val result = MutableList(normalizedSecretWord.length) { LetterStatus.INCORRECT }
        val secretWordLetterCounts = normalizedSecretWord.groupingBy { it }.eachCount().toMutableMap()

        upperGuess.forEachIndexed { index, char ->
            if (index < normalizedSecretWord.length && char == normalizedSecretWord[index]) {
                result[index] = LetterStatus.CORRECT
                secretWordLetterCounts[char] = secretWordLetterCounts.getOrDefault(char, 0) - 1
            }
        }

        upperGuess.forEachIndexed { index, char ->
            if (result[index] == LetterStatus.INCORRECT) {
                if (secretWordLetterCounts.getOrDefault(char, 0) > 0) {
                    result[index] = LetterStatus.WRONG_POSITION
                    secretWordLetterCounts[char] = secretWordLetterCounts.getOrDefault(char, 0) - 1
                }
            }
        }
        return result
    }
}