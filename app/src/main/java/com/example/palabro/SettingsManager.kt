package com.example.palabro

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.palabro.ui.theme.ThemeStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserSettings(
    val themeStyle: ThemeStyle,
    val wordLength: Int
)

class SettingsManager private constructor(private val context: Context) {

    companion object {
        // --- INICIO DEL CAMBIO ---
        // Esto crea una única instancia de SettingsManager para toda la app (Patrón Singleton)
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        // --- FIN DEL CAMBIO ---

        val THEME_KEY = stringPreferencesKey("theme_preference")
        val WORD_LENGTH_KEY = intPreferencesKey("word_length")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: ThemeStyle.SYSTEM.name
        val wordLength = preferences[WORD_LENGTH_KEY] ?: 5

        UserSettings(
            themeStyle = ThemeStyle.valueOf(themeName),
            wordLength = wordLength
        )
    }

    suspend fun setTheme(theme: ThemeStyle) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun setWordLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[WORD_LENGTH_KEY] = length
        }
    }
}