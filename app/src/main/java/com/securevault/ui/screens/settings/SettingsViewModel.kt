// app/src/main/java/com/securevault/ui/screens/settings/SettingsViewModel.kt
package com.securevault.ui.screens.settings

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.securevault.MainActivity
import com.securevault.data.model.BackupResult
import com.securevault.data.model.RestoreResult
import com.securevault.di.AppModule
import com.securevault.domain.usecase.CreateBackupUseCase
import com.securevault.domain.usecase.DeleteAllPasswordsUseCase
import com.securevault.domain.usecase.RestoreBackupUseCase
import com.securevault.utils.BackupLocationInfo
import com.securevault.utils.BackupManager
import com.securevault.utils.EnhancedBackupResult
import com.securevault.utils.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val biometricHelper = AppModule.provideBiometricHelper(application)
    private val themeManager = AppModule.provideThemeManager(application)

    // Enhanced backup manager with external storage support
    private val backupManager = AppModule.provideBackupManager(application)
    private val createBackupUseCase = AppModule.provideCreateBackupUseCase(application)
    private val restoreBackupUseCase = AppModule.provideRestoreBackupUseCase(application)
    private val deleteAllPasswordsUseCase = AppModule.provideDeleteAllPasswordsUseCase(application)

    // UI States
    val themeConfig: StateFlow<ThemeManager.ThemeConfig> = themeManager.currentTheme

    // Temporary theme config for batch changes
    private val _tempThemeConfig = MutableStateFlow(themeManager.currentTheme.value)
    val tempThemeConfig: StateFlow<ThemeManager.ThemeConfig> = _tempThemeConfig.asStateFlow()

    // Track if there are unsaved changes
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(
        biometricHelper.isBiometricEnabled()
    )
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Enhanced backup/restore states
    private val _isBackupLoading = MutableStateFlow(false)
    val isBackupLoading: StateFlow<Boolean> = _isBackupLoading.asStateFlow()

    private val _isRestoreLoading = MutableStateFlow(false)
    val isRestoreLoading: StateFlow<Boolean> = _isRestoreLoading.asStateFlow()

    private val _selectedBackupUri = MutableStateFlow<Uri?>(null)
    val selectedBackupUri: StateFlow<Uri?> = _selectedBackupUri.asStateFlow()

    private val _selectedBackupFileName = MutableStateFlow<String?>(null)
    val selectedBackupFileName: StateFlow<String?> = _selectedBackupFileName.asStateFlow()

    // Backup location info
    private val _backupLocationInfo = MutableStateFlow<BackupLocationInfo?>(null)
    val backupLocationInfo: StateFlow<BackupLocationInfo?> = _backupLocationInfo.asStateFlow()

    // Last created backup info for sharing
    private val _lastBackupPath = MutableStateFlow<String?>(null)
    val lastBackupPath: StateFlow<String?> = _lastBackupPath.asStateFlow()

    init {
        // Update temp config when actual config changes
        viewModelScope.launch {
            themeManager.currentTheme.collect { config ->
                _tempThemeConfig.value = config
                checkForUnsavedChanges()
            }
        }

        // Load backup location info
        loadBackupLocationInfo()
    }

    private fun loadBackupLocationInfo() {
        viewModelScope.launch {
            _backupLocationInfo.value = backupManager.getBackupLocationInfo()
        }
    }

    private fun checkForUnsavedChanges() {
        val current = themeManager.currentTheme.value
        val temp = _tempThemeConfig.value
        _hasUnsavedChanges.value = current != temp
    }

    // Theme management methods (unchanged)
    fun toggleDarkMode() {
        val current = _tempThemeConfig.value
        _tempThemeConfig.value = current.copy(isDark = !current.isDark)
        checkForUnsavedChanges()

        setThemeMode(ThemeManager.THEME_MODE_BASIC)
        saveAndApplyChanges()
    }

    fun setThemeMode(mode: String) {
        val current = _tempThemeConfig.value
        _tempThemeConfig.value = current.copy(mode = mode)
        checkForUnsavedChanges()
    }

    fun updateBackgroundColor(color: Color) {
        val current = _tempThemeConfig.value
        _tempThemeConfig.value = current.copy(backgroundColor = color)
        setThemeMode(ThemeManager.THEME_MODE_ADVANCED)
        checkForUnsavedChanges()
    }

    fun updateSurfaceColor(color: Color) {
        val current = _tempThemeConfig.value
        _tempThemeConfig.value = current.copy(surfaceColor = color)
        setThemeMode(ThemeManager.THEME_MODE_ADVANCED)
        checkForUnsavedChanges()
    }

    fun updatePrimaryColor(color: Color) {
        val current = _tempThemeConfig.value
        _tempThemeConfig.value = current.copy(primaryColor = color)
        setThemeMode(ThemeManager.THEME_MODE_ADVANCED)
        checkForUnsavedChanges()
    }

    fun updateTextColor(color: Color) {
        val current = _tempThemeConfig.value
        _tempThemeConfig.value = current.copy(textColor = color)
        setThemeMode(ThemeManager.THEME_MODE_ADVANCED)
        checkForUnsavedChanges()
    }

    fun resetTheme() {
        val defaultConfig = ThemeManager.ThemeConfig(
            mode = ThemeManager.THEME_MODE_BASIC,
            isDark = false,
            backgroundColor = Color(0xFFFFFFFF),
            surfaceColor = Color(0xFFF5F5F5),
            primaryColor = Color(0xFF1976D2),
            textColor = Color(0xFF000000)
        )
        _tempThemeConfig.value = defaultConfig
        checkForUnsavedChanges()
    }

    fun discardChanges() {
        _tempThemeConfig.value = themeManager.currentTheme.value
        _hasUnsavedChanges.value = false
    }

    fun saveAndApplyChanges() {
        themeManager.saveThemeConfig(_tempThemeConfig.value)
        _hasUnsavedChanges.value = false
    }

    fun applyThemeChanges(context: Context) {
        if (context is Activity) {
            context.runOnUiThread {
                val intent = Intent(context, MainActivity::class.java).apply {
                    putExtra(MainActivity.EXTRA_IS_THEME_CHANGE, true)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                context.finish()
            }
        }
    }

    fun saveAndApplyThemeChanges(context: Context) {
        saveAndApplyChanges()
        applyThemeChanges(context)
    }

    // Security methods
    fun setBiometricEnabled(enabled: Boolean) {
        biometricHelper.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    // Enhanced backup methods
    fun createBackup(password: String, deleteAfterBackup: Boolean = false) {
        viewModelScope.launch {
            try {
                _isBackupLoading.value = true
                _error.value = null

                val result = backupManager.createBackup(password)

                when (result) {
                    is EnhancedBackupResult.Success -> {
                        _lastBackupPath.value = result.externalPath ?: result.internalPath

                        val locationMsg = if (result.isExternalAvailable) {
                            "Backup saved to Downloads/SecureVault_Backups"
                        } else {
                            "Backup saved to internal storage"
                        }

                        _successMessage.value = "Backup created successfully with ${result.passwordCount} passwords. $locationMsg"

                        // Delete all passwords if requested
                        if (deleteAfterBackup) {
                            val deleteResult = deleteAllPasswordsUseCase()
                            if (deleteResult) {
                                _successMessage.value = "Backup created and all passwords deleted from device. $locationMsg"
                            } else {
                                _error.value = "Backup created but failed to delete passwords from device"
                            }
                        }
                    }
                    is EnhancedBackupResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isBackupLoading.value = false
            }
        }
    }

    fun createBackupAndShare(password: String, deleteAfterBackup: Boolean = false) {
        viewModelScope.launch {
            try {
                _isBackupLoading.value = true
                _error.value = null

                val result = backupManager.createBackup(password)

                when (result) {
                    is EnhancedBackupResult.Success -> {
                        val sharePath = result.externalPath ?: result.internalPath
                        _lastBackupPath.value = sharePath

                        _successMessage.value = "Backup created with ${result.passwordCount} passwords. Ready to share."

                        // Delete all passwords if requested
                        if (deleteAfterBackup) {
                            val deleteResult = deleteAllPasswordsUseCase()
                            if (!deleteResult) {
                                _error.value = "Backup created but failed to delete passwords from device"
                            }
                        }
                    }
                    is EnhancedBackupResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isBackupLoading.value = false
            }
        }
    }

    fun createBackupToLocation(password: String, destinationUri: Uri, deleteAfterBackup: Boolean = false) {
        viewModelScope.launch {
            try {
                _isBackupLoading.value = true
                _error.value = null

                val result = backupManager.createBackupToUri(password, destinationUri)

                when (result) {
                    is BackupResult.Success -> {
                        _successMessage.value = "Backup created successfully with ${result.passwordCount} passwords at selected location"

                        // Delete all passwords if requested
                        if (deleteAfterBackup) {
                            val deleteResult = deleteAllPasswordsUseCase()
                            if (!deleteResult) {
                                _error.value = "Backup created but failed to delete passwords from device"
                            }
                        }
                    }
                    is BackupResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isBackupLoading.value = false
            }
        }
    }

    fun getShareIntent(): Intent? {
        val backupPath = _lastBackupPath.value ?: return null
        return backupManager.createShareIntent(backupPath)
    }

    fun restoreBackup(password: String, replaceAll: Boolean = true) {
        viewModelScope.launch {
            try {
                _isRestoreLoading.value = true
                _error.value = null

                val uri = _selectedBackupUri.value
                if (uri == null) {
                    _error.value = "Please select a backup file first"
                    return@launch
                }

                val result = restoreBackupUseCase.restoreFromUri(uri, password, replaceAll)

                when (result) {
                    is RestoreResult.Success -> {
                        _successMessage.value = "Successfully restored ${result.restoredCount}/${result.passwordCount} passwords"
                        // Clear selected file after successful restore
                        _selectedBackupUri.value = null
                        _selectedBackupFileName.value = null
                    }
                    is RestoreResult.Error -> {
                        _error.value = result.message
                    }
                    is RestoreResult.InvalidPassword -> {
                        _error.value = "Invalid backup password. Please try again."
                    }
                    is RestoreResult.InvalidFile -> {
                        _error.value = "Invalid backup file format: ${result.message}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isRestoreLoading.value = false
            }
        }
    }

    fun selectBackupFile(uri: Uri, fileName: String) {
        _selectedBackupUri.value = uri
        _selectedBackupFileName.value = fileName
    }

    fun clearSelectedBackupFile() {
        _selectedBackupUri.value = null
        _selectedBackupFileName.value = null
    }

    // Message handling
    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun getBackupLocationDescription(): String {
        val info = _backupLocationInfo.value ?: return "Internal storage"
        return if (info.isExternalAvailable) {
            "Downloads/SecureVault_Backups"
        } else {
            "Internal storage only"
        }
    }
}