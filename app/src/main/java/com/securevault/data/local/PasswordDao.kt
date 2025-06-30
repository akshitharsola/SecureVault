// app/src/main/java/com/securevault/data/local/PasswordDao.kt
package com.securevault.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM passwords ORDER BY updatedAt ASC")
    fun getAllPasswordsFlow(): Flow<List<PasswordEntity>>

    @Query("SELECT * FROM passwords ORDER BY updatedAt ASC")
    suspend fun getAllPasswords(): List<PasswordEntity>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: String): PasswordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: PasswordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPasswords(passwords: List<PasswordEntity>)

    @Update
    suspend fun updatePassword(password: PasswordEntity)

    @Query("DELETE FROM passwords WHERE id = :id")
    suspend fun deletePasswordById(id: String): Int

    @Query("DELETE FROM passwords")
    suspend fun deleteAllPasswords()

    @Query("SELECT * FROM passwords WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%' ORDER BY updatedAt ASC")
    suspend fun searchPasswords(query: String): List<PasswordEntity>

    @Query("SELECT COUNT(*) FROM passwords")
    suspend fun getPasswordCount(): Int
}