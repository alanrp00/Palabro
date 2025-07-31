package com.example.palabro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val statsManager = remember { StatsManager.getInstance(context) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val wordLengths = listOf(5, 6, 7)
    val selectedLength = wordLengths[selectedTabIndex]

    val stats by statsManager.getStatsFlow(selectedLength).collectAsState(
        initial = GameStats(0, 0, 0, 0)
    )

    val winPercentage = if (stats.gamesPlayed > 0) {
        (stats.wins.toFloat() / stats.gamesPlayed.toFloat() * 100).toInt()
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            wordLengths.forEachIndexed { index, length ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = "$length Letras") }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                StatRow(label = "Partidas Jugadas", value = stats.gamesPlayed.toString())
                HorizontalDivider()
                StatRow(label = "Victorias", value = stats.wins.toString())
                HorizontalDivider()
                StatRow(label = "Porcentaje de Victorias", value = "$winPercentage%")
                HorizontalDivider()
                StatRow(label = "Racha Actual", value = stats.currentStreak.toString())
                HorizontalDivider()
                StatRow(label = "Mejor Racha", value = stats.maxStreak.toString())
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}