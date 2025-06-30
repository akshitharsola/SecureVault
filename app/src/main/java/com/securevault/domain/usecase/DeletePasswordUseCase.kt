// app/src/main/java/com/example/securevault/domain/usecase/DeletePasswordUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.repository.PasswordRepository

class DeletePasswordUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(id: String): Boolean {
        return repository.deletePassword(id)
    }
}