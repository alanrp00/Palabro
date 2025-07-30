package com.example.palabro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager.getInstance(application)

    private lateinit var gameLogic: GameLogic

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val secretWord: String
        get() = if (::gameLogic.isInitialized) gameLogic.secretWord else ""

    init {
        viewModelScope.launch {
            val initialWordLength = settingsManager.settingsFlow.first().wordLength
            gameLogic = GameLogic(application, initialWordLength)
            _uiState.value = GameUiState(wordLength = initialWordLength)
        }
    }

    fun onKey(key: String) {
        if (uiState.value.gameStatus != GameStatus.PLAYING) return

        when (key) {
            "ENTER" -> submitGuess()
            "DELETE" -> {
                if (uiState.value.currentGuess.isNotEmpty()) {
                    _uiState.update { it.copy(currentGuess = it.currentGuess.dropLast(1)) }
                }
            }
            else -> {
                if (uiState.value.currentGuess.length < gameLogic.wordLength) {
                    _uiState.update { it.copy(currentGuess = it.currentGuess + key) }
                }
            }
        }
    }

    private fun submitGuess() {
        val guess = uiState.value.currentGuess
        if (guess.length != gameLogic.wordLength) return

        if (gameLogic.isValidWord(guess)) {
            val statuses = gameLogic.submitGuess(guess)
            val newSubmittedGuesses = uiState.value.submittedGuesses + Guess(guess, statuses)

            val newKeyStatuses = uiState.value.keyStatuses.toMutableMap()
            guess.uppercase().forEachIndexed { index, char ->
                val currentStatus = newKeyStatuses[char]
                val newStatus = statuses[index]
                if (currentStatus == null || newStatus == LetterStatus.CORRECT || (currentStatus != LetterStatus.CORRECT && newStatus != LetterStatus.INCORRECT)) {
                    newKeyStatuses[char] = newStatus
                }
            }

            val newGameStatus = when {
                statuses.all { it == LetterStatus.CORRECT } -> GameStatus.WON
                newSubmittedGuesses.size >= gameLogic.maxAttempts -> GameStatus.LOST
                else -> GameStatus.PLAYING
            }

            _uiState.update { currentState ->
                currentState.copy(
                    submittedGuesses = newSubmittedGuesses,
                    currentGuess = "",
                    keyStatuses = newKeyStatuses,
                    gameStatus = newGameStatus
                )
            }
        } else {
            // TODO: Shake animation
        }
    }

    fun resetGame() {
        gameLogic.startNewGame()
        _uiState.update { it.copy(
            submittedGuesses = emptyList(),
            currentGuess = "",
            keyStatuses = emptyMap(),
            gameStatus = GameStatus.PLAYING,
            revealedHints = emptyMap()
        ) }
    }

    fun changeWordLength(newLength: Int) {
        viewModelScope.launch {
            settingsManager.setWordLength(newLength)
            gameLogic = GameLogic(getApplication(), newLength)
            _uiState.value = GameUiState(wordLength = newLength)
        }
    }

    fun onHintPressed() {
        _uiState.update { it.copy(showHintDialog = true) }
    }

    fun onHintConfirm() {
        // Pasamos el mapa de pistas actual a la lógica
        val hint = gameLogic.getHint(uiState.value.currentGuess, uiState.value.revealedHints)

        if (hint != null) {
            val (index, letter) = hint
            // Actualizamos el mapa de pistas, añadiendo la nueva
            val newHints = uiState.value.revealedHints + (index to letter)
            _uiState.update {
                it.copy(
                    revealedHints = newHints,
                    showHintDialog = false
                )
            }
        } else {
            _uiState.update { it.copy(showHintDialog = false) }
        }
    }

    fun onHintDismiss() {
        _uiState.update { it.copy(showHintDialog = false) }
    }
}

data class GameUiState(
    val wordLength: Int = 5,
    val submittedGuesses: List<Guess> = emptyList(),
    val currentGuess: String = "",
    val keyStatuses: Map<Char, LetterStatus> = emptyMap(),
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val showHintDialog: Boolean = false,
    val revealedHints: Map<Int, Char> = emptyMap() // <-- AÑADE ESTA LÍNEA
)