// app/src/main/java/com/securevault/ui/screens/main/MainViewModel.kt
package com.securevault.ui.screens.main

import android.app.Application
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val getPasswordsUseCase = AppModule.provideGetPasswordsUseCase(application)
    private val deletePasswordUseCase = AppModule.provideDeletePasswordUseCase(application)
    private val searchPasswordsUseCase = AppModule.provideSearchPasswordsUseCase(application)
    private val deleteAllPasswordsUseCase = AppModule.provideDeleteAllPasswordsUseCase(application)
    private val biometricHelper = AppModule.provideBiometricHelper(application)


    // UI states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(!biometricHelper.isBiometricEnabled())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Get password flow from use case
    val passwords = getPasswordsUseCase().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Filtered passwords based on search query
    val filteredPasswords = combine(passwords, _searchQuery) { passwordList, query ->
        if (query.isBlank()) {
            passwordList
        } else {
            val lowercaseQuery = query.lowercase()
            passwordList.filter { password ->
                password.title.lowercase().contains(lowercaseQuery) ||
                        password.username.lowercase().contains(lowercaseQuery)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAuthenticated(value: Boolean) {
        _isAuthenticated.value = value
    }

    fun isBiometricAuthNeeded(): Boolean {
        return biometricHelper.isBiometricEnabled() && biometricHelper.canAuthenticate()
    }

    fun deletePassword(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = deletePasswordUseCase(id)
                if (!success) {
                    _error.value = "Failed to delete password"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // New function to delete all passwords with biometric authentication
    fun deleteAllPasswords(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Always authenticate before deleting all passwords
        if (biometricHelper.isBiometricEnabled() && biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate(
                activity = activity,
                title = "Delete All Passwords",
                subtitle = "Please authenticate to delete all passwords",
                onSuccess = {
                    performDeleteAllPasswords(onSuccess)
                },
                onError = onError
            )
        } else {
            // No biometric authentication available, proceed directly
            performDeleteAllPasswords(onSuccess)
        }
    }

    // Helper function to perform the actual deletion
    private fun performDeleteAllPasswords(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = deleteAllPasswordsUseCase()
                if (success) {
                    onSuccess()
                } else {
                    _error.value = "Failed to delete all passwords"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPasswords() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Force a refresh
                val result = getPasswordsUseCase()
            } catch (e: Exception) {
                _error.value = "Error refreshing passwords: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    init {
        refreshPasswords()

        // Debug logging
        viewModelScope.launch {
            passwords.collect { passwordList ->
                Log.d("MainViewModel", "Current passwords: ${passwordList.size}")
                passwordList.forEach {
                    Log.d("MainViewModel", "Password in list: ${it.id} - ${it.title}")
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}