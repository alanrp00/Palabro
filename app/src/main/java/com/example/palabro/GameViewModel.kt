package com.example.palabro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
                } else { return }
            }
        }
        val guess = String(fullWordChars)

        if (gameLogic.isValidWord(guess)) {
            val statuses = gameLogic.submitGuess(guess)
            val newSubmittedGuesses = uiState.value.submittedGuesses + Guess(guess, statuses, isRevealed = false)
            val newKeyStatuses = uiState.value.keyStatuses.toMutableMap()
            guess.uppercase().forEachIndexed { index, char ->
                val currentStatus = newKeyStatuses[char]
                val newStatus = statuses[index]
                if (currentStatus == null || newStatus == LetterStatus.CORRECT || (currentStatus != LetterStatus.CORRECT && newStatus != LetterStatus.INCORRECT)) {
                    newKeyStatuses[char] = newStatus
                }
            }

            // --- INICIO DE LA CORRECCIÓN ---

            val isWin = statuses.all { it == LetterStatus.CORRECT }
            val isLoss = newSubmittedGuesses.size >= gameLogic.maxAttempts

            // 1. Actualizamos el tablero inmediatamente para que la animación de volteo comience.
            //    Mantenemos el estado como PLAYING por ahora.
            val finalSubmittedGuesses = newSubmittedGuesses.mapIndexed { index, oldGuess ->
                if (index == newSubmittedGuesses.lastIndex) oldGuess.copy(isRevealed = true) else oldGuess
            }
            _uiState.update { currentState ->
                currentState.copy(
                    submittedGuesses = finalSubmittedGuesses,
                    currentGuess = "",
                    keyStatuses = newKeyStatuses,
                    revealedHints = emptyMap()
                )
            }

            // 2. Si el juego ha terminado (victoria o derrota), lanzamos una corrutina con retraso.
            if (isWin || isLoss) {
                viewModelScope.launch {
                    // Calculamos la duración total de la animación de volteo
                    val animationDuration = (gameLogic.wordLength * 300L) + 600L // 300ms por letra + un extra

                    // Esperamos a que la animación termine
                    delay(animationDuration)

                    // Actualizamos las estadísticas
                    if (isWin) {
                        statsManager.incrementWins(gameLogic.wordLength)
                    } else {
                        statsManager.incrementLosses(gameLogic.wordLength)
                    }

                    // Finalmente, actualizamos el estado para mostrar el diálogo de resultado
                    _uiState.update {
                        it.copy(gameStatus = if (isWin) GameStatus.WON else GameStatus.LOST)
                    }
                }
            }
            // --- FIN DE LA CORRECCIÓN ---

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