package com.example.palabro

import android.content.Context
import android.util.Log
import kotlin.random.Random

class GameLogic(private val context: Context, val wordLength: Int) {

    private val tag = "GameLogic"

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
        // Añadimos un log para depurar qué se está validando exactamente.
        val normalizedWord = word.uppercase().normalize()
        val isValid = normalizedWordSet.contains(normalizedWord)
        Log.d(tag, "Validando: '$normalizedWord'. Es válido: $isValid")
        return isValid
    }

    fun startNewGame() {
        if (wordList.isNotEmpty()) {
            secretWord = wordList.random(Random(System.currentTimeMillis()))
            guesses.clear()
            Log.d(tag, "La palabra secreta ($wordLength letras) es: $secretWord")
        }
    }

    // --- FUNCIÓN submitGuess CORREGIDA ---
    fun submitGuess(guess: String): List<LetterStatus> {
        val upperGuess = guess.uppercase()
        // Guardamos siempre el intento del usuario tal cual, sin normalizar.
        guesses.add(upperGuess)

        val normalizedSecret = secretWord.normalize()
        val normalizedGuess = upperGuess.normalize()

        val result = MutableList(wordLength) { LetterStatus.INCORRECT }
        val secretLetterCounts = normalizedSecret.groupingBy { it }.eachCount().toMutableMap()

        // 1. Comprobamos las letras CORRECTAS (posición y letra coinciden)
        for (i in 0 until wordLength) {
            if (normalizedGuess[i] == normalizedSecret[i]) {
                result[i] = LetterStatus.CORRECT
                secretLetterCounts[normalizedGuess[i]] = secretLetterCounts.getOrDefault(normalizedGuess[i], 0) - 1
            }
        }

        // 2. Comprobamos las letras en POSICIÓN INCORRECTA
        for (i in 0 until wordLength) {
            if (result[i] == LetterStatus.INCORRECT) { // Solo si no la hemos marcado ya como correcta
                if (secretLetterCounts.getOrDefault(normalizedGuess[i], 0) > 0) {
                    result[i] = LetterStatus.WRONG_POSITION
                    secretLetterCounts[normalizedGuess[i]] = secretLetterCounts.getOrDefault(normalizedGuess[i], 0) - 1
                }
            }
        }
        return result
    }
    // --- FIN DE LA CORRECCIÓN ---


    fun getHint(currentGuess: String, revealedHints: Map<Int, Char>): Pair<Int, Char>? {
        val unguessedIndices = secretWord.indices.filter { index ->
            val notGuessedCorrectly = guesses.all { guess ->
                // Comparamos la letra de la palabra secreta con la letra (normalizada) del intento
                secretWord[index].normalize() != guess.normalize().getOrNull(index)
            }
            val notInCurrentGuess = currentGuess.normalize().getOrNull(index) != secretWord[index].normalize()
            val notAlreadyRevealed = !revealedHints.containsKey(index)

            notGuessedCorrectly && notInCurrentGuess && notAlreadyRevealed
        }

        if (unguessedIndices.isEmpty()) {
            return null
        }

        val randomIndex = unguessedIndices.random()
        return Pair(randomIndex, secretWord[randomIndex])
    }
}