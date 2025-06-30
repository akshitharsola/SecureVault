// app/src/main/java/com/securevault/MainActivity.kt
package com.securevault

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.securevault.di.AppModule
import com.securevault.ui.components.PinEntryDialog
import com.securevault.ui.navigation.AppNavigation
import com.securevault.ui.theme.SecureAttendTheme

class MainActivity : FragmentActivity() {

    companion object {
        const val EXTRA_IS_THEME_CHANGE = "isThemeChange"
        const val KEY_IS_AUTHENTICATED = "isAuthenticated"
        const val KEY_THEME_CHANGE_FLAG = "themeChangeFlag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this is a theme change from intent or saved state
        val isThemeChangeFromIntent = intent.getBooleanExtra(EXTRA_IS_THEME_CHANGE, false)
        val isThemeChangeFromSavedState = savedInstanceState?.getBoolean(KEY_THEME_CHANGE_FLAG, false) ?: false
        val wasAuthenticated = savedInstanceState?.getBoolean(KEY_IS_AUTHENTICATED, false) ?: false

        val isThemeChange = isThemeChangeFromIntent || isThemeChangeFromSavedState

        // Clear the theme change flag from intent to prevent it from persisting
        if (isThemeChangeFromIntent) {
            intent.removeExtra(EXTRA_IS_THEME_CHANGE)
        }

        setContent {
            SecureAttendTheme {
                // Auto-authenticate if theme change or was previously authenticated
                var isAuthenticated by remember { mutableStateOf(isThemeChange || wasAuthenticated) }
                var showPinDialog by remember { mutableStateOf(false) }
                val biometricHelper = AppModule.provideBiometricHelper(applicationContext)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        AppNavigation()
                    }

                    // Show PIN dialog if needed
                    if (showPinDialog) {
                        PinEntryDialog(
                            onDismiss = {
                                showPinDialog = false
                                finish()
                            },
                            onPinEntered = { pin ->
                                if (biometricHelper.verifyFallbackPin(pin)) {
                                    isAuthenticated = true
                                    showPinDialog = false
                                } else {
                                    Toast.makeText(this@MainActivity, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // Only authenticate on first launch, not on theme changes or if already authenticated
                LaunchedEffect(Unit) {
                    if (!isThemeChange && !wasAuthenticated && biometricHelper.isBiometricEnabled() && biometricHelper.canAuthenticate()) {
                        biometricHelper.authenticate(
                            activity = this@MainActivity,
                            title = "Authenticate to Access",
                            subtitle = "Please verify your identity to access your passwords",
                            onSuccess = {
                                isAuthenticated = true
                            },
                            onError = { errorMsg ->
                                if (biometricHelper.isFallbackPinSet()) {
                                    Toast.makeText(this@MainActivity, "Biometric failed. Please enter PIN", Toast.LENGTH_SHORT).show()
                                    showPinDialog = true
                                } else {
                                    Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        )
                    } else if (!biometricHelper.isBiometricEnabled()) {
                        // If biometric is disabled, allow access without authentication
                        isAuthenticated = true
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the theme change flag and authentication state
        outState.putBoolean(KEY_THEME_CHANGE_FLAG, intent.getBooleanExtra(EXTRA_IS_THEME_CHANGE, false))
        outState.putBoolean(KEY_IS_AUTHENTICATED, true) // If we're saving state, user was authenticated
    }
}