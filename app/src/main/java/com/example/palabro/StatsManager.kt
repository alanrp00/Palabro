package com.example.palabro

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stats_by_mode")

data class GameStats(
    val gamesPlayed: Int,
    val wins: Int,
    val currentStreak: Int,
    val maxStreak: Int
)

class StatsManager(private val context: Context) {

    // CAMBIO: Las funciones ahora necesitan la longitud de la palabra
    // para saber qué estadísticas leer o modificar.
    fun getStatsFlow(length: Int): Flow<GameStats> {
        return context.dataStore.data.map { preferences ->
            val gamesPlayed = preferences[intPreferencesKey("games_played_$length")] ?: 0
            val wins = preferences[intPreferencesKey("wins_$length")] ?: 0
            val currentStreak = preferences[intPreferencesKey("current_streak_$length")] ?: 0
            val maxStreak = preferences[intPreferencesKey("max_streak_$length")] ?: 0
            GameStats(gamesPlayed, wins, currentStreak, maxStreak)
        }
    }

    suspend fun incrementWins(length: Int) {
        context.dataStore.edit { preferences ->
            val gamesPlayedKey = intPreferencesKey("games_played_$length")
            val winsKey = intPreferencesKey("wins_$length")
            val currentStreakKey = intPreferencesKey("current_streak_$length")
            val maxStreakKey = intPreferencesKey("max_streak_$length")

            val currentGamesPlayed = preferences[gamesPlayedKey] ?: 0
            preferences[gamesPlayedKey] = currentGamesPlayed + 1

            val currentWins = preferences[winsKey] ?: 0
            preferences[winsKey] = currentWins + 1

            val currentStreak = preferences[currentStreakKey] ?: 0
            val newStreak = currentStreak + 1
            preferences[currentStreakKey] = newStreak

            val maxStreak = preferences[maxStreakKey] ?: 0
            preferences[maxStreakKey] = max(newStreak, maxStreak)
        }
    }

    suspend fun incrementLosses(length: Int) {
        context.dataStore.edit { preferences ->
            val gamesPlayedKey = intPreferencesKey("games_played_$length")
            val currentStreakKey = intPreferencesKey("current_streak_$length")

            val currentGamesPlayed = preferences[gamesPlayedKey] ?: 0
            preferences[gamesPlayedKey] = currentGamesPlayed + 1

            preferences[currentStreakKey] = 0
        }
    }
}