package com.example.palabro

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define un DataStore para la app.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stats")

// Una clase de datos para agrupar nuestras estadísticas.
data class GameStats(val gamesPlayed: Int, val wins: Int)

class StatsManager(private val context: Context) {

    // Creamos las "llaves" para acceder a nuestros datos.
    companion object {
        val GAMES_PLAYED_KEY = intPreferencesKey("games_played")
        val WINS_KEY = intPreferencesKey("wins")
    }

    // Creamos un "Flow" que nos notificará cada vez que las estadísticas cambien.
    val statsFlow: Flow<GameStats> = context.dataStore.data.map { preferences ->
        val gamesPlayed = preferences[GAMES_PLAYED_KEY] ?: 0
        val wins = preferences[WINS_KEY] ?: 0
        GameStats(gamesPlayed, wins)
    }

    // Función para incrementar las victorias y las partidas jugadas.
    suspend fun incrementWins() {
        context.dataStore.edit { preferences ->
            val currentGamesPlayed = preferences[GAMES_PLAYED_KEY] ?: 0
            preferences[GAMES_PLAYED_KEY] = currentGamesPlayed + 1

            val currentWins = preferences[WINS_KEY] ?: 0
            preferences[WINS_KEY] = currentWins + 1
        }
    }

    // Función para incrementar solo las partidas jugadas (cuando se pierde).
    suspend fun incrementLosses() {
        context.dataStore.edit { preferences ->
            val currentGamesPlayed = preferences[GAMES_PLAYED_KEY] ?: 0
            preferences[GAMES_PLAYED_KEY] = currentGamesPlayed + 1
        }
    }
}