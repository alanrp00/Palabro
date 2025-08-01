package com.example.palabro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.palabro.ui.theme.LocalGameColors

/**
 * Una casilla individual para mostrar una letra del título.
 */
@Composable
private fun TitleLetterBox(letter: Char, color: Color) {
    Surface(
        modifier = Modifier.size(38.dp), // Un tamaño más pequeño que las del juego
        color = color,
        shape = RoundedCornerShape(8.dp) // Usamos un radio más pequeño
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * El título completo del juego "Palabro" estilizado.
 */
@Composable
fun GameTitle() {
    val title = "PALABRO"
    val gameColors = LocalGameColors.current

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        title.forEachIndexed { index, letter ->
            // La última letra ('O') tiene el color de "incorrecto", el resto "correcto".
            val color = if (index == title.lastIndex) {
                gameColors.incorrect
            } else {
                gameColors.correct
            }
            TitleLetterBox(letter = letter, color = color)
        }
    }
}