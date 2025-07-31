package com.example.palabro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palabro.ui.theme.LocalGameColors

@Composable
fun GameScreen(gameViewModel: GameViewModel) {
    val uiState by gameViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        WordLengthSelector(
            selectedLength = uiState.wordLength,
            onLengthSelected = { gameViewModel.changeWordLength(it) }
        )

        GameBoard(
            wordLength = uiState.wordLength,
            submittedGuesses = uiState.submittedGuesses,
            currentGuess = uiState.currentGuess,
            shakeOffset = 0f,
            uiState = uiState
        )

        GameKeyboard(
            keyStatuses = uiState.keyStatuses,
            onKeyClick = { gameViewModel.onKey(it) }
        )
    }

    if (uiState.gameStatus != GameStatus.PLAYING) {
        GameResultDialog(
            status = uiState.gameStatus,
            secretWord = gameViewModel.secretWord,
            onDismiss = { gameViewModel.resetGame() }
        )
    }

    if (uiState.showHintDialog) {
        AlertDialog(
            onDismissRequest = { gameViewModel.onHintDismiss() },
            title = { Text("Pista") },
            text = { Text("Revelar una letra correcta?") },
            confirmButton = {
                TextButton(onClick = { gameViewModel.onHintConfirm() }) {
                    Text("Revelar")
                }
            },
            dismissButton = {
                TextButton(onClick = { gameViewModel.onHintDismiss() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun WordLengthSelector(
    selectedLength: Int,
    onLengthSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(5, 6, 7).forEach { length ->
            val isSelected = selectedLength == length
            OutlinedButton(
                onClick = { onLengthSelected(length) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(text = "$length Letras")
            }
        }
    }
}

@Composable
fun LetterBox(
    letter: Char?,
    status: LetterStatus?,
    isActiveRow: Boolean,
    isCurrentLetter: Boolean,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    val color = when (status) {
        LetterStatus.CORRECT -> gameColors.correct
        LetterStatus.WRONG_POSITION -> gameColors.wrongPosition
        LetterStatus.INCORRECT -> gameColors.incorrect
        null -> MaterialTheme.colorScheme.background
    }

    val borderColor = when {
        isActiveRow && isCurrentLetter -> MaterialTheme.colorScheme.onBackground
        isActiveRow && letter != null -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
    }

    val finalBorder = if (status != null) BorderStroke(2.dp, Color.Transparent) else BorderStroke(2.dp, borderColor)

    Surface(
        modifier = modifier.aspectRatio(1f),
        color = color,
        border = finalBorder,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (letter != null) {
                val textColor = if (status != null) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
                Text(
                    // --- INICIO DEL CAMBIO ---
                    text = letter.normalize().toString(), // ¡Aquí aplicamos la magia!
                    // --- FIN DEL CAMBIO ---
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun GameBoard(
    wordLength: Int,
    submittedGuesses: List<Guess>,
    currentGuess: String,
    shakeOffset: Float,
    uiState: GameUiState
) {
    val maxAttempts = 6
    val currentAttemptIndex = submittedGuesses.size

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until maxAttempts) {
            val (word, statuses) = when {
                i < currentAttemptIndex -> submittedGuesses[i]
                i == currentAttemptIndex -> Guess(word = currentGuess, statuses = null)
                else -> Guess(word = "", statuses = null)
            }

            val rowOffset = if (i == currentAttemptIndex) {
                (kotlin.math.sin(shakeOffset * 2 * Math.PI) * 15).toFloat()
            } else 0f

            WordRow(
                wordLength = wordLength,
                word = word,
                statuses = statuses,
                isActiveRow = (i == currentAttemptIndex),
                modifier = Modifier.offset(x = rowOffset.dp),
                revealedHints = uiState.revealedHints // <-- AÑADE ESTA LÍNEA
            )
        }
    }
}

@Composable
fun WordRow(
    wordLength: Int,
    word: String,
    statuses: List<LetterStatus>?,
    isActiveRow: Boolean,
    modifier: Modifier = Modifier,
    revealedHints: Map<Int, Char> = emptyMap()
) {
    // Puntero para saber qué letra de la palabra del usuario (word) toca poner.
    var guessPointer = 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until wordLength) {
            val letter: Char?
            var status: LetterStatus? = null

            if (isActiveRow) {
                // --- INICIO DE LA NUEVA LÓGICA ---
                if (revealedHints.containsKey(i)) {
                    // Si la celda actual (i) tiene una pista, la mostramos.
                    letter = revealedHints[i]
                    status = LetterStatus.CORRECT // La pintamos de verde
                } else {
                    // Si no tiene una pista, es una celda para el usuario.
                    // Usamos el puntero para coger la siguiente letra que ha escrito.
                    letter = word.getOrNull(guessPointer)
                    status = null
                    // Si hemos puesto una letra, avanzamos el puntero.
                    if (letter != null) {
                        guessPointer++
                    }
                }
                // --- FIN DE LA NUEVA LÓGICA ---
            } else {
                // Para las filas ya enviadas, la lógica no cambia.
                letter = word.getOrNull(i)
                status = statuses?.getOrNull(i)
            }

            // --- LÓGICA DEL CURSOR ACTUALIZADA ---
            // Buscamos la primera celda que no tenga pista. Esa es la siguiente posición libre.
            val nextFreeSlot = (0 until wordLength).firstOrNull { j -> !revealedHints.containsKey(j) } ?: 0
            // El cursor estará en la primera celda libre que venga después de las letras ya escritas.
            val cursorPosition = (nextFreeSlot until wordLength)
                .find { j -> !revealedHints.containsKey(j) && j >= guessPointer } ?: guessPointer

            val isCurrentLetter = isActiveRow && i == cursorPosition

            LetterBox(
                letter = letter,
                status = status,
                isActiveRow = isActiveRow,
                isCurrentLetter = isCurrentLetter,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KeyboardKey(
    key: String,
    status: LetterStatus?,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gameColors = LocalGameColors.current
    val color = when (status) {
        LetterStatus.CORRECT -> gameColors.correct
        LetterStatus.WRONG_POSITION -> gameColors.wrongPosition
        LetterStatus.INCORRECT -> gameColors.incorrect
        null -> MaterialTheme.colorScheme.surface
    }
    val contentColor = if (status != null) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onKeyClick(key) },
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (key) {
                "ENTER" -> Icon(Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = "Enter", tint = contentColor)
                "DELETE" -> Icon(Icons.Filled.Backspace, contentDescription = "Borrar", tint = contentColor)
                else -> Text(
                    text = key,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun GameKeyboard(keyStatuses: Map<Char, LetterStatus>, onKeyClick: (String) -> Unit) {
    val row1 = "QWERTYUIOP"
    val row2 = "ASDFGHJKLÑ"
    val row3 = "ZXCVBNM"

    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KeyboardRow(keys = row1, keyStatuses = keyStatuses, onKeyClick = onKeyClick)
        KeyboardRow(keys = row2, keyStatuses = keyStatuses, onKeyClick = onKeyClick)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KeyboardKey(key = "ENTER", status = null, onKeyClick = onKeyClick, modifier = Modifier.weight(1.5f))
            KeyboardRow(keys = row3, keyStatuses = keyStatuses, onKeyClick = onKeyClick, modifier = Modifier.weight(7f))
            KeyboardKey(key = "DELETE", status = null, onKeyClick = onKeyClick, modifier = Modifier.weight(1.5f))
        }
    }
}

@Composable
fun KeyboardRow(
    keys: String,
    keyStatuses: Map<Char, LetterStatus>,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keys.forEach { char ->
            KeyboardKey(
                key = char.toString(),
                status = keyStatuses[char],
                onKeyClick = onKeyClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GameResultDialog(status: GameStatus, secretWord: String, onDismiss: () -> Unit) {
    val title = if (status == GameStatus.WON) "¡Has Ganado!" else "¡Has Perdido!"
    val message = "La palabra era: $secretWord"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Jugar de Nuevo")
            }
        }
    )
}