// app/src/main/java/com/securevault/data/local/PasswordEntity.kt
package com.securevault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.securevault.data.model.Password

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toPassword(): Password {
        return Password(
            id = id,
            title = title,
            username = username,
            password = password,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromPassword(password: Password): PasswordEntity {
            return PasswordEntity(
                id = password.id,
                title = password.title,
                username = password.username,
                password = password.password,
                notes = password.notes,
                createdAt = password.createdAt,
                updatedAt = password.updatedAt
            )
        }
    }
}