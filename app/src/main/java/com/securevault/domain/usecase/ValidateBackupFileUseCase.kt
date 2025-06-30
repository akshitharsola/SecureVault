// app/src/main/java/com/securevault/domain/usecase/ValidateBackupFileUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.model.BackupData
import com.securevault.utils.BackupManager

class ValidateBackupFileUseCase(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(
        filePath: String,
        password: String
    ): BackupData? {
        if (password.isBlank()) {
            return null
        }

        return backupManager.validateBackup(filePath, password)
    }
}