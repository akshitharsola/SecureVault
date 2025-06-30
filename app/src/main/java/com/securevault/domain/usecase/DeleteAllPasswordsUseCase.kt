// app/src/main/java/com/securevault/domain/usecase/DeleteAllPasswordsUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.repository.PasswordRepository

class DeleteAllPasswordsUseCase(private val repository: PasswordRepository) {

    suspend operator fun invoke(): Boolean {
        return try {
            repository.deleteAllPasswords()
            true
        } catch (e: Exception) {
            false
        }
    }
}