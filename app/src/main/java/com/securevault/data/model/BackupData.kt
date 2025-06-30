// app/src/main/java/com/securevault/data/model/BackupData.kt
package com.securevault.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the structure of backup files
 * Compatible with iOS implementation for cross-platform backup/restore
 */
data class BackupData(
    @SerializedName("version")
    val version: String = "1.0",

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("encrypted")
    val encrypted: Boolean = true,

    @SerializedName("passwordCount")
    val passwordCount: Int,

    @SerializedName("appName")
    val appName: String = "SecureVault",

    @SerializedName("platform")
    val platform: String = "Android",

    @SerializedName("data")
    val data: String // Base64 encoded encrypted JSON string of passwords
)

/**
 * Represents the result of backup operations
 */
sealed class BackupResult {
    data class Success(val filePath: String, val passwordCount: Int) : BackupResult()
    data class Error(val message: String, val exception: Throwable? = null) : BackupResult()
}

/**
 * Represents the result of restore operations
 */
sealed class RestoreResult {
    data class Success(val passwordCount: Int, val restoredCount: Int) : RestoreResult()
    data class Error(val message: String, val exception: Throwable? = null) : RestoreResult()
    data class InvalidPassword(val message: String = "Invalid backup password") : RestoreResult()
    data class InvalidFile(val message: String = "Invalid backup file format") : RestoreResult()
}