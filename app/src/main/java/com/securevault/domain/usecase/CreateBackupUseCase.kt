// app/src/main/java/com/securevault/domain/usecase/CreateBackupUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.model.BackupResult
import com.securevault.utils.BackupManager
import com.securevault.utils.EnhancedBackupResult

class CreateBackupUseCase(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(
        password: String,
        fileName: String? = null
    ): BackupResult {
        if (password.isBlank()) {
            return BackupResult.Error("Backup password cannot be empty")
        }

        if (password.length < 6) {
            return BackupResult.Error("Backup password must be at least 6 characters long")
        }

        // Convert EnhancedBackupResult to BackupResult
        return when (val result = backupManager.createBackup(password, fileName)) {
            is EnhancedBackupResult.Success -> {
                val filePath = result.externalPath ?: result.internalPath
                BackupResult.Success(filePath, result.passwordCount)
            }
            is EnhancedBackupResult.Error -> {
                BackupResult.Error(result.message, result.exception)
            }
        }
    }
}