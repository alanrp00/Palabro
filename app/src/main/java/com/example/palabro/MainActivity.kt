package com.example.palabro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.palabro.ui.theme.PalabroTheme
import com.example.palabro.ui.theme.ThemeStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val settings by settingsManager.settingsFlow.collectAsState(
                initial = UserSettings(ThemeStyle.SYSTEM, 5)
            )

            PalabroTheme(themeStyle = settings.themeStyle) {
                AppNavigation()
            }
        }
    }
}