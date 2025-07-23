package com.example.palabro

import android.content.Context
import kotlin.random.Random

enum class LetterStatus {
    CORRECT,
    WRONG_POSITION,
    INCORRECT
}

class GameLogic(private val context: Context, val wordLength: Int) {

    private var wordList: List<String> = emptyList()

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
            wordList = lines.map { it.uppercase() }.toList()
        }
    }

    // CAMBIO: Nueva función para comprobar si la palabra existe en la lista.
    fun isValidWord(word: String): Boolean {
        return wordList.contains(word.uppercase())
    }

    fun startNewGame() {
        if (wordList.isNotEmpty()) {
            secretWord = wordList.random(Random(System.currentTimeMillis()))
            guesses.clear()
            println("La palabra secreta ($wordLength letras) es: $secretWord")
        }
    }

    fun submitGuess(guess: String): List<LetterStatus> {
        val upperGuess = guess.uppercase()
        guesses.add(upperGuess)

        val result = MutableList(secretWord.length) { LetterStatus.INCORRECT }
        val secretWordLetterCounts = secretWord.groupingBy { it }.eachCount().toMutableMap()

        upperGuess.forEachIndexed { index, char ->
            if (index < secretWord.length && char == secretWord[index]) {
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