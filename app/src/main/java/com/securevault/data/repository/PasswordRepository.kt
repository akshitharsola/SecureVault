// app/src/main/java/com/securevault/data/repository/PasswordRepository.kt
package com.securevault.data.repository

import com.securevault.data.model.Password
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAllPasswords(): Flow<List<Password>>
    suspend fun getPassword(id: String): Password?
    suspend fun savePassword(password: Password): Boolean
    suspend fun deletePassword(id: String): Boolean
    suspend fun deleteAllPasswords(): Boolean
    suspend fun searchPasswords(query: String): List<Password>

    // New methods for backup/restore operations
    suspend fun getAllPasswordsList(): List<Password>
    suspend fun savePasswords(passwords: List<Password>): Boolean
    suspend fun replaceAllPasswords(passwords: List<Password>): Boolean
}