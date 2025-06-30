// app/src/main/java/com/example/securevault/domain/usecase/GetPasswordsUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.model.Password
import com.securevault.data.repository.PasswordRepository

class GetPasswordUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(id: String): Password? {
        return repository.getPassword(id)
    }
}