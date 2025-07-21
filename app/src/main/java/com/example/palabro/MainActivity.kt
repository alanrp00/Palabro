package com.example.palabro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palabro.ui.theme.PalabroTheme

// Un data class para guardar un intento junto con el estado de sus letras.
data class Guess(val word: String, val statuses: List<LetterStatus>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PalabroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen()
                }
            }
        }
    }
}

@Composable
fun GameScreen() {
    val gameLogic = remember { GameLogic() }
    var currentGuess by remember { mutableStateOf("") }
    // Esta es la lista principal que guarda los intentos YA REALIZADOS.
    var submittedGuesses by remember { mutableStateOf<List<Guess>>(emptyList()) }

    // --- ESTA FUNCIÓN SE CONECTARÁ AL TECLADO ---
    // Se encarga de procesar un intento cuando el jugador lo envía.
    val onSubmitGuess = {
        if (currentGuess.length == 5 && submittedGuesses.size < gameLogic.maxAttempts) {
            val statuses = gameLogic.submitGuess(currentGuess)
            submittedGuesses = submittedGuesses + Guess(word = currentGuess, statuses = statuses)
            currentGuess = "" // Limpiamos el intento actual
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Palabro",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        GameBoard(
            submittedGuesses = submittedGuesses,
            currentGuess = currentGuess
        )

        // Aquí añadiremos el teclado en el siguiente paso.
        // Y lo conectaremos a la función `onSubmitGuess`.
    }
}

@Composable
fun GameBoard(submittedGuesses: List<Guess>, currentGuess: String) {
    val maxAttempts = 6

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Dibuja las filas de intentos ya realizados. Ahora es mucho más sencillo.
        submittedGuesses.forEach { guess ->
            WordRow(word = guess.word, statuses = guess.statuses)
        }

        // Dibuja la fila del intento actual (si no hemos agotado los intentos)
        if (submittedGuesses.size < maxAttempts) {
            WordRow(word = currentGuess, statuses = null)
        }

        // Dibuja las filas vacías restantes
        repeat(maxAttempts - submittedGuesses.size - 1) {
            if (it >= 0) { // Pequeña guarda para evitar valores negativos
                WordRow(word = "", statuses = null)
            }
        }
    }
}

@Composable
fun WordRow(word: String, statuses: List<LetterStatus>?) {
    val wordLength = 5
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0 until wordLength) {
            val letter = word.getOrNull(i)
            val status = statuses?.getOrNull(i)
            LetterBox(letter = letter, status = status)
        }
    }
}

@Composable
fun LetterBox(letter: Char?, status: LetterStatus?) {
    val color = when (status) {
        LetterStatus.CORRECT -> Color(0xFF6AAA64) // Verde
        LetterStatus.WRONG_POSITION -> Color(0xFFC9B458) // Amarillo
        LetterStatus.INCORRECT -> Color.DarkGray
        null -> Color.Transparent
    }

    val border = if (status == null && letter == null) BorderStroke(1.dp, Color.LightGray)
    else if (status == null && letter != null) BorderStroke(1.dp, Color.DarkGray)
    else null

    Surface(
        modifier = Modifier.size(60.dp),
        color = color,
        border = border,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (letter != null) {
                Text(
                    text = letter.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    PalabroTheme {
        GameScreen()
    }
}