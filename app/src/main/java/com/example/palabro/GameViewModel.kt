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
    private val statsManager = StatsManager.getInstance(application)

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
        val fullWordChars = CharArray(gameLogic.wordLength)
        var guessPointer = 0
        for (i in 0 until gameLogic.wordLength) {
            if (uiState.value.revealedHints.containsKey(i)) {
                fullWordChars[i] = uiState.value.revealedHints.getValue(i)
            } else {
                if (guessPointer < uiState.value.currentGuess.length) {
                    fullWordChars[i] = uiState.value.currentGuess[guessPointer]
                    guessPointer++
                } else {
                    return
                }
            }
        }
        val guess = String(fullWordChars)


        if (gameLogic.isValidWord(guess)) {
            val statuses = gameLogic.submitGuess(guess)
            // Creamos el nuevo intento, marcándolo como NO revelado inicialmente.
            val newSubmittedGuesses = uiState.value.submittedGuesses + Guess(guess, statuses, isRevealed = false)

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

            if (newGameStatus != GameStatus.PLAYING) {
                viewModelScope.launch {
                    when (newGameStatus) {
                        GameStatus.WON -> statsManager.incrementWins(gameLogic.wordLength)
                        GameStatus.LOST -> statsManager.incrementLosses(gameLogic.wordLength)
                        else -> {}
                    }
                }
            }

            // Marcamos el último intento como revelado para disparar la animación
            val finalSubmittedGuesses = newSubmittedGuesses.mapIndexed { index, oldGuess ->
                if (index == newSubmittedGuesses.lastIndex) oldGuess.copy(isRevealed = true) else oldGuess
            }

            _uiState.update { currentState ->
                currentState.copy(
                    submittedGuesses = finalSubmittedGuesses, // Usamos la nueva lista
                    currentGuess = "",
                    keyStatuses = newKeyStatuses,
                    gameStatus = newGameStatus,
                    revealedHints = emptyMap()
                )
            }
        } else {
            _uiState.update { it.copy(triggerShake = it.triggerShake + 1) }
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

    // Dentro de la clase GameViewModel

    fun onHintConfirm() {
        val hint = gameLogic.getHint(uiState.value.currentGuess, uiState.value.revealedHints)

        if (hint != null) {
            val (index, letter) = hint
            val newHints = uiState.value.revealedHints + (index to letter)

            // --- INICIO DE LA CORRECCIÓN ---
            // Actualizamos también el estado de la tecla en el teclado
            val newKeyStatuses = uiState.value.keyStatuses.toMutableMap()
            newKeyStatuses[letter] = LetterStatus.CORRECT
            // --- FIN DE LA CORRECCIÓN ---

            _uiState.update {
                it.copy(
                    revealedHints = newHints,
                    showHintDialog = false,
                    currentGuess = "",
                    keyStatuses = newKeyStatuses // Usamos el nuevo mapa de estados de teclas
                )
            }
        } else {
            _uiState.update { it.copy(showHintDialog = false) }
        }
    }

    fun onHintDismiss() {
        _uiState.update { it.copy(showHintDialog = false) }
    }

    fun onShakeAnimationCompleted() {
        _uiState.update { it.copy(triggerShake = 0) }
    }
}

data class GameUiState(
    val wordLength: Int = 5,
    val submittedGuesses: List<Guess> = emptyList(),
    val currentGuess: String = "",
    val keyStatuses: Map<Char, LetterStatus> = emptyMap(),
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val showHintDialog: Boolean = false,
    val revealedHints: Map<Int, Char> = emptyMap(),
    val triggerShake: Int = 0
)