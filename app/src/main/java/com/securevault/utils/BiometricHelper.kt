// app/src/main/java/com/securevault/utils/BiometricHelper.kt
package com.securevault.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricHelper(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

    private val TAG = "BiometricHelper"

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_FALLBACK_PIN = "fallback_pin"
    }

    fun isBiometricEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication succeeded")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e(TAG, "Authentication error: $errString")
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.e(TAG, "Authentication failed - face/fingerprint not recognized")
                // Don't call onError here to allow user to retry
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching biometric prompt: ${e.message}")
            onError("Failed to launch authentication: ${e.message}")
        }
    }

    fun setFallbackPin(pin: String) {
        // In a real app, this would be securely stored
        sharedPreferences.edit().putString(KEY_FALLBACK_PIN, pin).apply()
    }

    fun verifyFallbackPin(pin: String): Boolean {
        val storedPin = sharedPreferences.getString(KEY_FALLBACK_PIN, null)
        return storedPin == pin
    }

    fun isFallbackPinSet(): Boolean {
        return !sharedPreferences.getString(KEY_FALLBACK_PIN, null).isNullOrEmpty()
    }
}