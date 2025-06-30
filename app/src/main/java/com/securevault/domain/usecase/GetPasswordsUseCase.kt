// app/src/main/java/com/securevault/domain/usecase/GetPasswordsUseCase.kt
package com.securevault.domain.usecase

import com.securevault.data.model.Password
import com.securevault.data.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow

class GetPasswordsUseCase(private val repository: PasswordRepository) {

    operator fun invoke(): Flow<List<Password>> {
        return repository.getAllPasswords()
    }
}