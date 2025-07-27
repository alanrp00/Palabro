package com.example.palabro

import android.content.Context
import android.util.Log
import kotlin.random.Random

private fun String.normalize(): String {
    return this.replace('Á', 'A')
        .replace('É', 'E')
        .replace('Í', 'I')
        .replace('Ó', 'O')
        .replace('Ú', 'U')
}

class GameLogic(private val context: Context, val wordLength: Int) {

    private val tag = "GameLogic" // Convención de Kotlin: minúsculas para tags privados

    private var wordList: List<String> = emptyList()
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
            normalizedWordSet = words.map { it.normalize() }.toSet()
        }
    }

    fun isValidWord(word: String): Boolean {
        return normalizedWordSet.contains(word.uppercase())
    }

    fun startNewGame() {
        if (wordList.isNotEmpty()) {
            secretWord = wordList.random(Random(System.currentTimeMillis()))
            guesses.clear()
            Log.d(tag, "La palabra secreta ($wordLength letras) es: $secretWord")
        }
    }

    fun submitGuess(guess: String): List<LetterStatus> {
        val upperGuess = guess.uppercase()
        guesses.add(upperGuess)

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