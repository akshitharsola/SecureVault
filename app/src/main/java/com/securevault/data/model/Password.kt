// app/src/main/java/com/example/securevault/data/model/Password.kt
package com.securevault.data.model

import java.util.UUID

data class Password(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val username: String,
    val password: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)