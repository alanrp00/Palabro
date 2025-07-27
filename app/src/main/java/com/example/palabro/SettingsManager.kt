package com.example.palabro

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

// Un objeto que contendrá todas las preferencias del usuario.
data class UserSettings(
    val themeStyle: ThemeStyle,
    val wordLength: Int
)

class SettingsManager(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_preference")
        val WORD_LENGTH_KEY = intPreferencesKey("word_length")
    }

    // Un único Flow que emite todas las preferencias a la vez.
    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: ThemeStyle.SYSTEM.name
        val wordLength = preferences[WORD_LENGTH_KEY] ?: 5 // Por defecto, 5 letras

        UserSettings(
            themeStyle = ThemeStyle.valueOf(themeName),
            wordLength = wordLength
        )
    }

    // Función para guardar la nueva preferencia de tema.
    suspend fun setTheme(theme: ThemeStyle) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    // Función para guardar la nueva longitud de palabra.
    suspend fun setWordLength(length: Int) {
        context.dataStore.edit { preferences ->
            preferences[WORD_LENGTH_KEY] = length
        }
    }
}