// app/src/main/java/com/securevault/ui/screens/detail/DetailViewModel.kt
package com.securevault.ui.screens.detail

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.data.model.Password
import com.securevault.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    application: Application,
    private val passwordId: String
) : AndroidViewModel(application) {

    private val getPasswordUseCase = AppModule.provideGetPasswordUseCase(application)
    private val deletePasswordUseCase = AppModule.provideDeletePasswordUseCase(application)
    private val biometricHelper = AppModule.provideBiometricHelper(application)
    private val clipboardManager = AppModule.provideClipboardManager(application)

    // UI states
    private val _password = MutableStateFlow<Password?>(null)
    val password: StateFlow<Password?> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadPassword()
    }

    private fun loadPassword() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getPasswordUseCase(passwordId)
                _password.value = result
                if (result == null) {
                    _error.value = "Password not found"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Always require authentication when showing password
    fun authenticateAndShowPassword(activity: FragmentActivity, onError: (String) -> Unit) {
        if (biometricHelper.isBiometricEnabled() && biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate(
                activity = activity,
                title = "Authentication Required",
                subtitle = "Please authenticate to view password",
                onSuccess = {
                    _isPasswordVisible.value = true
                },
                onError = onError
            )
        } else {
            // No biometric auth needed or available
            _isPasswordVisible.value = true
        }
    }

    // For hiding password, no auth needed
    fun hidePassword() {
        _isPasswordVisible.value = false
    }

    fun copyToClipboard(label: String, text: String) {
        clipboardManager.copyToClipboard(label, text)
    }

    fun authenticateAndDeletePassword(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (biometricHelper.isBiometricEnabled() && biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate(
                activity = activity,
                title = "Authentication Required",
                subtitle = "Please authenticate to delete this password",
                onSuccess = {
                    performDeletePassword(onSuccess)
                },
                onError = onError
            )
        } else {
            // No biometric auth needed or available
            performDeletePassword(onSuccess)
        }
    }

    private fun performDeletePassword(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = deletePasswordUseCase(passwordId)
                if (success) {
                    onSuccess()
                } else {
                    _error.value = "Failed to delete password"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePassword(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = deletePasswordUseCase(passwordId)
                if (success) {
                    onSuccess()
                } else {
                    _error.value = "Failed to delete password"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}