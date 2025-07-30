package com.example.palabro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El ViewModel ahora necesita el "contexto" de la aplicación para poder crear GameLogic.
// Por eso heredamos de AndroidViewModel en lugar de ViewModel.
class GameViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia de la lógica del juego, ahora vive dentro del ViewModel.
    // La inicializamos con 5 letras por defecto, luego la cambiaremos.
    private var gameLogic = GameLogic(application, 5)

    // --- ESTADO DEL JUEGO (STATE) ---
    // Usamos StateFlow para que la UI (la pantalla) pueda observar los cambios.
    // El guion bajo (_) indica que es una variable privada y mutable.
    private val _uiState = MutableStateFlow(GameUiState())
    // Esta es la versión pública e inmutable que la UI podrá leer.
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val secretWord: String
        get() = gameLogic.secretWord

    // --- MANEJADORES DE EVENTOS (EVENTS) ---
    // Esta función será llamada cuando el usuario pulse una tecla.
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

    // Función para procesar un intento.
    private fun submitGuess() {
        val guess = uiState.value.currentGuess
        if (guess.length != gameLogic.wordLength) return

        if (gameLogic.isValidWord(guess)) {
            val statuses = gameLogic.submitGuess(guess)
            val newSubmittedGuesses = uiState.value.submittedGuesses + Guess(guess, statuses)

            // Actualizar el estado de las teclas del teclado
            val newKeyStatuses = uiState.value.keyStatuses.toMutableMap()
            guess.uppercase().forEachIndexed { index, char ->
                val currentStatus = newKeyStatuses[char]
                val newStatus = statuses[index]
                if (currentStatus == null || newStatus == LetterStatus.CORRECT || (currentStatus != LetterStatus.CORRECT && newStatus != LetterStatus.INCORRECT)) {
                    newKeyStatuses[char] = newStatus
                }
            }

            // Comprobar si el juego ha terminado (victoria o derrota)
            val newGameStatus = when {
                statuses.all { it == LetterStatus.CORRECT } -> GameStatus.WON
                newSubmittedGuesses.size >= gameLogic.maxAttempts -> GameStatus.LOST
                else -> GameStatus.PLAYING
            }

            // Actualizar todo el estado de la UI de una vez
            _uiState.update { currentState ->
                currentState.copy(
                    submittedGuesses = newSubmittedGuesses,
                    currentGuess = "",
                    keyStatuses = newKeyStatuses,
                    gameStatus = newGameStatus
                )
            }
        } else {
            // TODO: Añadir la lógica para la animación de vibración (shake)
        }
    }

    // Función para reiniciar el juego
    fun resetGame() {
        gameLogic.startNewGame()
        _uiState.value = GameUiState(wordLength = gameLogic.wordLength) // Reinicia al estado inicial
    }

    // Función para cambiar la longitud de la palabra
    fun changeWordLength(newLength: Int) {
        gameLogic = GameLogic(getApplication(), newLength)
        resetGame()
    }
}


// --- DATA CLASS PARA EL ESTADO DE LA UI ---
// Agrupamos todas las variables de estado en una sola clase.
// Esto hace que el código sea más limpio y fácil de mantener.
data class GameUiState(
    val wordLength: Int = 5,
    val submittedGuesses: List<Guess> = emptyList(),
    val currentGuess: String = "",
    val keyStatuses: Map<Char, LetterStatus> = emptyMap(),
    val gameStatus: GameStatus = GameStatus.PLAYING
)