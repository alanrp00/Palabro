package com.example.palabro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palabro.ui.theme.PalabroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Guess(val word: String, val statuses: List<LetterStatus>?)
enum class GameStatus { PLAYING, WON, LOST }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkTheme = isSystemInDarkTheme()
            PalabroTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GameScreen()
                }
            }
        }
    }
}

// CAMBIO PRINCIPAL: La lógica del borde ahora está en LetterBox y depende de si
// es la siguiente letra a escribir.
@Composable
fun LetterBox(
    letter: Char?,
    status: LetterStatus?,
    isActiveRow: Boolean,
    isCurrentLetter: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        LetterStatus.CORRECT -> Color(0xFF6AAA64)
        LetterStatus.WRONG_POSITION -> Color(0xFFC9B458)
        LetterStatus.INCORRECT -> Color(0xFF787C7E)
        null -> MaterialTheme.colorScheme.background
    }

    val borderColor = when {
        // CAMBIO AQUÍ: Usamos el color del texto del tema en lugar del primario.
        isActiveRow && isCurrentLetter -> MaterialTheme.colorScheme.onBackground

        // El resto de la lógica no cambia
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
                    text = letter.toString(),
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
    shakeOffset: Float
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
                modifier = Modifier.offset(x = rowOffset.dp)
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until wordLength) {
            val letter = word.getOrNull(i)
            val status = statuses?.getOrNull(i)
            // Calculamos si esta es la siguiente letra a escribir
            val isCurrentLetter = isActiveRow && i == word.length

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


// --- El resto del código no ha cambiado ---
@Composable
fun GameScreen() {
    val wordLength = 5
    val context = LocalContext.current
    val gameLogic = remember(wordLength) { GameLogic(context, wordLength) }

    val statsManager = remember { StatsManager(context) }
    val stats by statsManager.statsFlow.collectAsState(initial = GameStats(0, 0))
    val scope = rememberCoroutineScope()

    var currentGuess by remember { mutableStateOf("") }
    var submittedGuesses by remember { mutableStateOf<List<Guess>>(emptyList()) }
    var gameStatus by remember { mutableStateOf(GameStatus.PLAYING) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var keyStatuses by remember { mutableStateOf<Map<Char, LetterStatus>>(emptyMap()) }

    val shakeController = remember { Animatable(0f) }
    var shakeTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            shakeController.animateTo(1f, tween(durationMillis = 500))
            shakeController.snapTo(0f)
        }
    }

    val resetGame = {
        gameLogic.startNewGame()
        submittedGuesses = emptyList()
        currentGuess = ""
        gameStatus = GameStatus.PLAYING
        keyStatuses = emptyMap()
    }

    val onSubmitGuess = {
        if (currentGuess.length == wordLength && gameStatus == GameStatus.PLAYING) {
            if (gameLogic.isValidWord(currentGuess)) {
                val statuses = gameLogic.submitGuess(currentGuess)
                submittedGuesses = submittedGuesses + Guess(word = currentGuess, statuses = statuses)

                val newKeyStatuses = keyStatuses.toMutableMap()
                currentGuess.uppercase().forEachIndexed { index, char ->
                    val currentStatus = newKeyStatuses[char]
                    val newStatus = statuses[index]
                    if (currentStatus == null || newStatus == LetterStatus.CORRECT ||
                        (currentStatus == LetterStatus.INCORRECT && newStatus != LetterStatus.INCORRECT)) {
                        newKeyStatuses[char] = newStatus
                    }
                }
                keyStatuses = newKeyStatuses

                if (statuses.all { it == LetterStatus.CORRECT }) {
                    gameStatus = GameStatus.WON
                    scope.launch { statsManager.incrementWins() }
                } else if (submittedGuesses.size >= gameLogic.maxAttempts) {
                    gameStatus = GameStatus.LOST
                    scope.launch { statsManager.incrementLosses() }
                }
                currentGuess = ""
            } else {
                shakeTrigger++
            }
        }
    }

    val onKeyEvent = { key: String ->
        if (gameStatus == GameStatus.PLAYING) {
            when (key) {
                "ENTER" -> onSubmitGuess()
                "DELETE" -> { if (currentGuess.isNotEmpty()) currentGuess = currentGuess.dropLast(1) }
                else -> {
                    if (currentGuess.length < wordLength) {
                        currentGuess += key
                        if (currentGuess.length == wordLength) {
                            scope.launch {
                                delay(200L)
                                onSubmitGuess()
                            }
                        }
                    }
                }
            }
        }
    }

    if (gameStatus != GameStatus.PLAYING) {
        GameResultDialog(
            status = gameStatus,
            secretWord = gameLogic.secretWord,
            onDismiss = resetGame
        )
    }
    if (showStatsDialog) {
        StatsDialog(stats = stats, onDismiss = { showStatsDialog = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Palabro",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = { showStatsDialog = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.BarChart, contentDescription = "Estadísticas", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        GameBoard(
            wordLength = wordLength,
            submittedGuesses = submittedGuesses,
            currentGuess = currentGuess,
            shakeOffset = shakeController.value
        )

        GameKeyboard(keyStatuses = keyStatuses, onKeyClick = onKeyEvent)
    }
}

@Composable
fun KeyboardKey(
    key: String,
    status: LetterStatus?,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        LetterStatus.CORRECT -> Color(0xFF6AAA64)
        LetterStatus.WRONG_POSITION -> Color(0xFFC9B458)
        LetterStatus.INCORRECT -> Color(0xFF787C7E)
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
                "ENTER" -> Icon(Icons.Filled.KeyboardReturn, contentDescription = "Enter", tint = contentColor)
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
@Composable
fun StatsDialog(stats: GameStats, onDismiss: () -> Unit) {
    val winPercentage = if (stats.gamesPlayed > 0) {
        (stats.wins.toFloat() / stats.gamesPlayed.toFloat() * 100).toInt()
    } else {
        0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Estadísticas", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Partidas Jugadas: ${stats.gamesPlayed}")
                Text("Victorias: ${stats.wins}")
                Text("Porcentaje de Victorias: $winPercentage%")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    PalabroTheme {
        GameScreen()
    }
}