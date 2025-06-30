// app/src/main/java/com/example/securevault/domain/usecase/SavePasswordUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.model.Password
import com.securevault.data.repository.PasswordRepository

class SavePasswordUseCase(private val repository: PasswordRepository) {
    suspend operator fun invoke(password: Password): Boolean {
        return repository.savePassword(password)
    }
}