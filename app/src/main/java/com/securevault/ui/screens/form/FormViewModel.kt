
// app/src/main/java/com/example/securevault/ui/screens/form/FormViewModel.kt
package com.securevault.ui.screens.form

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.data.model.Password
import com.securevault.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FormViewModel(
    application: Application,
    private val passwordId: String? = null
) : AndroidViewModel(application) {

    private val getPasswordUseCase = AppModule.provideGetPasswordUseCase(application)
    private val savePasswordUseCase = AppModule.provideSavePasswordUseCase(application)

    // Form fields
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    // Form validation errors
    private val _titleError = MutableStateFlow<String?>(null)
    val titleError: StateFlow<String?> = _titleError.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // UI states
    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (passwordId != null) {
            _isEditMode.value = true
            loadPassword(passwordId)
        }
    }

    private fun loadPassword(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val password = getPasswordUseCase(id)
                password?.let {
                    _title.value = it.title
                    _username.value = it.username
                    _password.value = it.password
                    _notes.value = it.notes
                } ?: run {
                    _error.value = "Password not found"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTitle(value: String) {
        _title.value = value
        validateTitle()
    }

    fun updateUsername(value: String) {
        _username.value = value
        validateUsername()
    }

    fun updatePassword(value: String) {
        _password.value = value
        validatePassword()
    }

    fun updateNotes(value: String) {
        _notes.value = value
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun validateTitle(): Boolean {
        return if (_title.value.isBlank()) {
            _titleError.value = "Title is required"
            false
        } else {
            _titleError.value = null
            true
        }
    }

    fun validateUsername(): Boolean {
        return if (_username.value.isBlank()) {
            _usernameError.value = "Username is required"
            false
        } else {
            _usernameError.value = null
            true
        }
    }

    fun validatePassword(): Boolean {
        return if (_password.value.isBlank()) {
            _passwordError.value = "Password is required"
            false
        } else {
            _passwordError.value = null
            true
        }
    }

    fun validateForm(): Boolean {
        val isTitleValid = validateTitle()
        val isUsernameValid = validateUsername()
        val isPasswordValid = validatePassword()

        return isTitleValid && isUsernameValid && isPasswordValid
    }

    // Add this property to your FormViewModel class
    private val currentPassword: StateFlow<Password> = MutableStateFlow(
        Password(
            id = passwordId ?: UUID.randomUUID().toString(),
            title = "",
            username = "",
            password = "",
            notes = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    ).asStateFlow()

    // Then modify your savePassword function to use the form values instead
    fun savePassword(onSuccess: () -> Unit) {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPassword = Password(
                    id = passwordId ?: UUID.randomUUID().toString(),
                    title = _title.value,
                    username = _username.value,
                    password = _password.value,
                    notes = _notes.value,
                    createdAt = if (isEditMode.value)
                        getPasswordUseCase(passwordId!!)?.createdAt ?: System.currentTimeMillis()
                    else System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val success = savePasswordUseCase(newPassword)
                if (success) {
                    // Log success
                    Log.d("FormViewModel", "Password saved successfully: ${newPassword.id}, ${newPassword.title}")
                    onSuccess()
                } else {
                    _error.value = "Failed to save password"
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