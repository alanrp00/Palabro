package com.example.palabro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palabro.ui.theme.LocalGameColors
import kotlinx.coroutines.delay

@Composable
fun GameScreen(gameViewModel: GameViewModel) {
    val uiState by gameViewModel.uiState.collectAsState()

    val shakeController = remember { Animatable(0f) }

    LaunchedEffect(uiState.triggerShake) {
        if (uiState.triggerShake > 0) {
            shakeController.animateTo(1f, animationSpec = tween(durationMillis = 600))
            shakeController.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WordLengthSelector(
            selectedLength = uiState.wordLength,
            onLengthSelected = { gameViewModel.changeWordLength(it) }
        )

        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            GameBoard(
                wordLength = uiState.wordLength,
                submittedGuesses = uiState.submittedGuesses,
                currentGuess = uiState.currentGuess,
                shakeOffset = shakeController.value,
                uiState = uiState
            )
        }

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
            text = { Text("Se revelará una letra correcta en el tablero.") },
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


// --- INICIO DE LA VERSIÓN FINAL DE LETTERBOX ---
@Composable
fun LetterBox(
    letter: Char?,
    status: LetterStatus?,
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    isActiveRow: Boolean,
    isCurrentLetter: Boolean
) {
    val gameColors = LocalGameColors.current
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            rotation.animateTo(180f, animationSpec = tween(durationMillis = 800))
        }
    }

    val revealedColor = when (status) {
        LetterStatus.CORRECT -> gameColors.correct
        LetterStatus.WRONG_POSITION -> gameColors.wrongPosition
        LetterStatus.INCORRECT -> gameColors.incorrect
        else -> MaterialTheme.colorScheme.background
    }

    val borderColor = when {
        isCurrentLetter -> MaterialTheme.colorScheme.onBackground
        isActiveRow && letter != null -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        letter == null -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val surfaceColor = if (isActiveRow && status != null) {
        // Para la pista en la fila activa, el color es inmediato
        revealedColor
    } else {
        // Para las filas enviadas, el color depende del giro
        if (rotation.value < 90f) MaterialTheme.colorScheme.background else revealedColor
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 8 * density
            },
        color = surfaceColor,
        border = BorderStroke(2.dp, if (status != null) Color.Transparent else borderColor),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (letter != null) {
                // --- INICIO DE LA CORRECCIÓN ---
                Text(
                    text = letter.normalize().toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    // El color del texto también depende del giro para estar sincronizado
                    color = if (rotation.value < 90f && status == null) MaterialTheme.colorScheme.onBackground else Color.White,
                    modifier = Modifier.graphicsLayer {
                        if (rotation.value >= 90f) {
                            rotationY = 180f
                        }
                    }
                )
                // --- FIN DE LA CORRECCIÓN ---
            }
        }
    }
}


@Composable
fun WordRow(
    guess: Guess,
    wordLength: Int,
    isActiveRow: Boolean,
    modifier: Modifier = Modifier,
    revealedHints: Map<Int, Char> = emptyMap()
) {
    val revealedStates = remember(wordLength) {
        mutableStateListOf<Boolean>().apply { addAll(List(wordLength) { false }) }
    }

    LaunchedEffect(guess.isRevealed) {
        if (guess.isRevealed) {
            for (i in 0 until wordLength) {
                delay(300L)
                revealedStates[i] = true
            }
        }
    }

    var guessPointer = 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until wordLength) {
            val letter: Char?
            val status: LetterStatus?

            if (isActiveRow) {
                if (revealedHints.containsKey(i)) {
                    letter = revealedHints[i]
                    status = LetterStatus.CORRECT
                } else {
                    letter = guess.word.getOrNull(guessPointer)
                    status = null
                    if (letter != null) {
                        guessPointer++
                    }
                }
            } else {
                letter = guess.word.getOrNull(i)
                status = guess.statuses?.getOrNull(i)
            }

            val nextFreeSlot = (0 until wordLength).firstOrNull { j -> !revealedHints.containsKey(j) } ?: 0
            val cursorPosition = (nextFreeSlot until wordLength)
                .find { j -> !revealedHints.containsKey(j) && j >= guessPointer } ?: guessPointer

            val isCurrentLetter = isActiveRow && i == cursorPosition

            LetterBox(
                letter = letter,
                status = status,
                isRevealed = revealedStates[i],
                modifier = Modifier.weight(1f),
                isActiveRow = isActiveRow,
                isCurrentLetter = isCurrentLetter
            )
        }
    }
}
// --- FIN DE LAS CORRECCIONES ---


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
            val guess = when {
                i < currentAttemptIndex -> submittedGuesses[i]
                i == currentAttemptIndex -> Guess(word = currentGuess, statuses = null)
                else -> Guess(word = "", statuses = null)
            }

            val rowOffset = if (i == currentAttemptIndex) {
                (kotlin.math.sin(shakeOffset * 4 * Math.PI) * 10).toFloat()
            } else 0f

            WordRow(
                guess = guess,
                wordLength = wordLength,
                isActiveRow = (i == currentAttemptIndex),
                modifier = Modifier.offset(x = rowOffset.dp),
                revealedHints = uiState.revealedHints
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