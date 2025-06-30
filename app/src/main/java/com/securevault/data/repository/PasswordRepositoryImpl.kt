// app/src/main/java/com/securevault/data/repository/PasswordRepositoryImpl.kt
package com.securevault.data.repository

import android.util.Log
import com.securevault.data.local.PasswordDao
import com.securevault.data.local.PasswordEntity
import com.securevault.data.model.Password
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepositoryImpl(
    private val passwordDao: PasswordDao
) : PasswordRepository {

    private val TAG = "PasswordRepository"

    override fun getAllPasswords(): Flow<List<Password>> {
        Log.d(TAG, "Getting passwords flow")
        return passwordDao.getAllPasswordsFlow().map { entities ->
            val passwords = entities.map { it.toPassword() }
            // Add detailed logging
            Log.d(TAG, "Flow emitting ${passwords.size} passwords")
            passwords.forEach {
                Log.d(TAG, "Password in flow: ${it.id}, title: ${it.title}, username: ${it.username}")
            }
            passwords
        }
    }

    override suspend fun getPassword(id: String): Password? {
        return passwordDao.getPasswordById(id)?.toPassword()
    }

    override suspend fun savePassword(password: Password): Boolean {
        return try {
            val entity = PasswordEntity.fromPassword(password)
            passwordDao.insertPassword(entity)
            Log.d(TAG, "Password saved successfully: ${password.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving password: ${e.message}")
            false
        }
    }

    override suspend fun deletePassword(id: String): Boolean {
        return try {
            val rowsDeleted = passwordDao.deletePasswordById(id)
            val success = rowsDeleted > 0
            Log.d(TAG, "Password deleted: $id, success: $success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting password: ${e.message}")
            false
        }
    }

    override suspend fun deleteAllPasswords(): Boolean {
        return try {
            passwordDao.deleteAllPasswords()
            Log.d(TAG, "All passwords deleted")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all passwords: ${e.message}")
            false
        }
    }

    override suspend fun searchPasswords(query: String): List<Password> {
        return try {
            if (query.isBlank()) {
                return passwordDao.getAllPasswords().map { it.toPassword() }
            }

            val results = passwordDao.searchPasswords(query)
            Log.d(TAG, "Search for '$query' returned ${results.size} results")
            results.map { it.toPassword() }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching passwords: ${e.message}")
            emptyList()
        }
    }

    // New methods for backup/restore operations
    override suspend fun getAllPasswordsList(): List<Password> {
        return try {
            val entities = passwordDao.getAllPasswords()
            val passwords = entities.map { it.toPassword() }
            Log.d(TAG, "Retrieved ${passwords.size} passwords as list")
            passwords
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all passwords list: ${e.message}")
            emptyList()
        }
    }

    override suspend fun savePasswords(passwords: List<Password>): Boolean {
        return try {
            val entities = passwords.map { PasswordEntity.fromPassword(it) }
            passwordDao.insertPasswords(entities)
            Log.d(TAG, "Batch saved ${passwords.size} passwords")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error batch saving passwords: ${e.message}")
            false
        }
    }

    override suspend fun replaceAllPasswords(passwords: List<Password>): Boolean {
        return try {
            // Delete all existing passwords first
            passwordDao.deleteAllPasswords()

            // Insert new passwords
            val entities = passwords.map { PasswordEntity.fromPassword(it) }
            passwordDao.insertPasswords(entities)

            Log.d(TAG, "Replaced all passwords with ${passwords.size} new passwords")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error replacing all passwords: ${e.message}")
            false
        }
    }
}