// app/src/main/java/com/securevault/utils/ThemeManager.kt
package com.securevault.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadThemeConfig())
    val currentTheme: StateFlow<ThemeConfig> = _currentTheme.asStateFlow()

    companion object {
        private const val TAG = "ThemeManager"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_IS_DARK = "is_dark"
        private const val KEY_BACKGROUND_COLOR = "background_color"
        private const val KEY_SURFACE_COLOR = "surface_color"
        private const val KEY_PRIMARY_COLOR = "primary_color"
        private const val KEY_TEXT_COLOR = "text_color"

        const val THEME_MODE_BASIC = "basic"
        const val THEME_MODE_ADVANCED = "advanced"
    }

    data class ThemeConfig(
        val mode: String = THEME_MODE_BASIC,
        val isDark: Boolean = false,
        val backgroundColor: Color = Color(0xFFFFFFFF),
        val surfaceColor: Color = Color(0xFFF5F5F5),
        val primaryColor: Color = Color(0xFF1976D2),
        val textColor: Color = Color(0xFF000000)
    )

    private fun loadThemeConfig(): ThemeConfig {
        val config = ThemeConfig(
            mode = sharedPreferences.getString(KEY_THEME_MODE, THEME_MODE_BASIC) ?: THEME_MODE_BASIC,
            isDark = sharedPreferences.getBoolean(KEY_IS_DARK, false),
            backgroundColor = Color(sharedPreferences.getInt(KEY_BACKGROUND_COLOR, 0xFFFFFFFF.toInt())),
            surfaceColor = Color(sharedPreferences.getInt(KEY_SURFACE_COLOR, 0xFFF5F5F5.toInt())),
            primaryColor = Color(sharedPreferences.getInt(KEY_PRIMARY_COLOR, 0xFF1976D2.toInt())),
            textColor = Color(sharedPreferences.getInt(KEY_TEXT_COLOR, 0xFF000000.toInt()))
        )
        Log.d(TAG, "Loading theme config: $config")
        return config
    }

    fun saveThemeConfig(config: ThemeConfig) {
        Log.d(TAG, "Saving theme config: $config")
        sharedPreferences.edit().apply {
            putString(KEY_THEME_MODE, config.mode)
            putBoolean(KEY_IS_DARK, config.isDark)
            putInt(KEY_BACKGROUND_COLOR, config.backgroundColor.toArgb())
            putInt(KEY_SURFACE_COLOR, config.surfaceColor.toArgb())
            putInt(KEY_PRIMARY_COLOR, config.primaryColor.toArgb())
            putInt(KEY_TEXT_COLOR, config.textColor.toArgb())
            apply()
        }
        _currentTheme.value = config
    }

    fun toggleDarkMode() {
        val current = _currentTheme.value
        val newConfig = current.copy(isDark = !current.isDark)
        Log.d(TAG, "Toggling dark mode. New isDark: ${newConfig.isDark}")
        saveThemeConfig(newConfig)
    }

    fun setThemeMode(mode: String) {
        val current = _currentTheme.value
        val newConfig = current.copy(mode = mode)
        Log.d(TAG, "Setting theme mode to: $mode")
        saveThemeConfig(newConfig)
    }
}