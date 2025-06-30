// app/src/main/java/com/example/securevault/domain/usecase/SearchPasswordsUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.model.Password
import com.securevault.data.repository.PasswordRepository

class SearchPasswordsUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(query: String): List<Password> {
        return repository.searchPasswords(query)
    }
}