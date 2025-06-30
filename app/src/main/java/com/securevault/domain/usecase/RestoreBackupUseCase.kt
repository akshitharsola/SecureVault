// app/src/main/java/com/securevault/domain/usecase/RestoreBackupUseCase.kt
package com.securevault.domain.usecase

import android.net.Uri
import com.securevault.data.model.RestoreResult
import com.securevault.utils.BackupManager

class RestoreBackupUseCase(
    private val backupManager: BackupManager
) {
    suspend fun restoreFromFile(
        filePath: String,
        password: String,
        replaceAll: Boolean = true
    ): RestoreResult {
        if (password.isBlank()) {
            return RestoreResult.Error("Backup password cannot be empty")
        }

        return backupManager.restoreFromFile(filePath, password, replaceAll)
    }

    suspend fun restoreFromUri(
        uri: Uri,
        password: String,
        replaceAll: Boolean = true
    ): RestoreResult {
        if (password.isBlank()) {
            return RestoreResult.Error("Backup password cannot be empty")
        }

        return backupManager.restoreFromUri(uri, password, replaceAll)
    }
}