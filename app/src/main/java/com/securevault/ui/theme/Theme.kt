// app/src/main/java/com/securevault/ui/theme/Theme.kt
package com.securevault.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.securevault.di.AppModule
import com.securevault.utils.ThemeManager

@Composable
fun SecureAttendTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = AppModule.provideThemeManager(context)
    val themeConfig by themeManager.currentTheme.collectAsState()

    Log.d("SecureAttendTheme", "Theme config: $themeConfig")

    val colorScheme = when {
        themeConfig.mode == ThemeManager.THEME_MODE_ADVANCED -> {
            Log.d("SecureAttendTheme", "Using advanced theme mode")
            if (themeConfig.isDark) {
                darkColorScheme(
                    primary = themeConfig.primaryColor,
                    background = themeConfig.backgroundColor,
                    surface = themeConfig.surfaceColor,
                    onBackground = themeConfig.textColor,
                    onSurface = themeConfig.textColor
                )
            } else {
                lightColorScheme(
                    primary = themeConfig.primaryColor,
                    background = themeConfig.backgroundColor,
                    surface = themeConfig.surfaceColor,
                    onBackground = themeConfig.textColor,
                    onSurface = themeConfig.textColor
                )
            }
        }
        themeConfig.isDark -> {
            Log.d("SecureAttendTheme", "Using dark theme")
            darkColorScheme(
                primary = Color(0xFF6650a4),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E)
            )
        }
        else -> {
            Log.d("SecureAttendTheme", "Using light theme")
            lightColorScheme(
                primary = Color(0xFF6650a4),
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFF5F5F5)
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Enable edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Get the window insets controller
            val windowInsetsController = WindowCompat.getInsetsController(window, view)

            // Configure system bar appearance (light/dark content) - this is the modern way
            windowInsetsController.isAppearanceLightStatusBars = !themeConfig.isDark
            windowInsetsController.isAppearanceLightNavigationBars = !themeConfig.isDark

            // Only set colors for older Android versions that need it
            // For newer versions, the system will handle this automatically with the theme
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.primary.toArgb()
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
                @Suppress("DEPRECATION")
                window.navigationBarColor = colorScheme.surface.toArgb()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}