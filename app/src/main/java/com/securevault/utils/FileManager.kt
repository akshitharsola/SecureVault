// app/src/main/java/com/securevault/utils/FileManager.kt
package com.securevault.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enhanced file manager with external storage and sharing capabilities
 */
class FileManager(private val context: Context) {

    companion object {
        private const val TAG = "FileManager"
        private const val BACKUP_DIRECTORY = "backups"
        private const val BACKUP_EXTENSION = ".backup"
        private const val BACKUP_PREFIX = "SecureVault_Backup_"
        private const val DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
        private const val EXTERNAL_BACKUP_DIR = "SecureVault/Backups"
    }

    /**
     * Gets the internal backup directory (for temporary storage)
     */
    private fun getInternalBackupDirectory(): File {
        val backupDir = File(context.filesDir, BACKUP_DIRECTORY)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }

    /**
     * Gets the external backup directory (accessible by file managers)
     */
    private fun getExternalBackupDirectory(): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+, use Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, "SecureVault_Backups").apply {
                if (!exists()) mkdirs()
            }
        } else {
            // For older versions, use external storage
            val externalDir = Environment.getExternalStorageDirectory()
            if (externalDir != null && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                File(externalDir, EXTERNAL_BACKUP_DIR).apply {
                    if (!exists()) mkdirs()
                }
            } else {
                null
            }
        }
    }

    /**
     * Generates a backup filename with timestamp
     */
    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        return "$BACKUP_PREFIX$timestamp$BACKUP_EXTENSION"
    }

    /**
     * Saves backup data to external storage (accessible by file managers)
     * @param data The backup data to save
     * @param fileName Optional custom filename, if null generates automatic name
     * @return The full path of the saved file
     */
    fun saveBackupToExternalStorage(data: String, fileName: String? = null): String {
        val actualFileName = fileName ?: generateBackupFileName()
        val externalDir = getExternalBackupDirectory()
            ?: throw FileOperationException("External storage not available")

        val file = File(externalDir, actualFileName)

        try {
            FileOutputStream(file).use { fos ->
                fos.write(data.toByteArray(Charsets.UTF_8))
            }

            Log.i(TAG, "Backup saved to external storage: ${file.absolutePath}")
            return file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving backup to external storage", e)
            throw FileOperationException("Failed to save backup file: ${e.message}", e)
        }
    }

    /**
     * Saves backup data to internal storage first, then copies to external
     * This provides both security and accessibility
     */
    fun saveBackupWithSharing(data: String, fileName: String? = null): BackupSaveResult {
        val actualFileName = fileName ?: generateBackupFileName()

        try {
            // Save to internal storage first (secure)
            val internalDir = getInternalBackupDirectory()
            val internalFile = File(internalDir, actualFileName)

            FileOutputStream(internalFile).use { fos ->
                fos.write(data.toByteArray(Charsets.UTF_8))
            }

            // Try to save to external storage (accessible)
            val externalPath = try {
                saveBackupToExternalStorage(data, actualFileName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not save to external storage: ${e.message}")
                null
            }

            Log.i(TAG, "Backup saved - Internal: ${internalFile.absolutePath}, External: $externalPath")

            return BackupSaveResult(
                internalPath = internalFile.absolutePath,
                externalPath = externalPath,
                fileName = actualFileName,
                isExternalAvailable = externalPath != null
            )

        } catch (e: IOException) {
            Log.e(TAG, "Error saving backup", e)
            throw FileOperationException("Failed to save backup file: ${e.message}", e)
        }
    }

    /**
     * Creates a shareable URI for a backup file
     */
    fun createShareableUri(filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: $filePath")
                return null
            }

            // Use FileProvider to create a shareable URI
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating shareable URI", e)
            null
        }
    }

    /**
     * Creates a share intent for backup files
     */
    fun createShareIntent(filePath: String): Intent? {
        val uri = createShareableUri(filePath) ?: return null

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SecureVault Backup")
            putExtra(Intent.EXTRA_TEXT, "SecureVault password backup file")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Saves backup directly to user-selected location using Storage Access Framework
     */
    fun saveBackupToUri(data: String, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data.toByteArray(Charsets.UTF_8))
            }
            Log.i(TAG, "Backup saved to user-selected location: $uri")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving backup to URI: $uri", e)
            false
        }
    }

    /**
     * Reads backup data from a file
     * @param filePath The path of the backup file
     * @return The backup data as string
     */
    fun readBackupFromFile(filePath: String): String {
        val file = File(filePath)

        if (!file.exists()) {
            throw FileOperationException("Backup file does not exist: $filePath")
        }

        try {
            return FileInputStream(file).use { fis ->
                fis.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading backup file", e)
            throw FileOperationException("Failed to read backup file: ${e.message}", e)
        }
    }

    /**
     * Reads backup data from a content URI (from document picker)
     * @param uri The URI of the selected backup file
     * @return The backup data as string
     */
    fun readBackupFromUri(uri: Uri): String {
        try {
            return context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            } ?: throw FileOperationException("Unable to open file from URI")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading backup from URI", e)
            throw FileOperationException("Failed to read backup file: ${e.message}", e)
        }
    }

    /**
     * Lists all backup files from both internal and external storage
     */
    fun listAllBackupFiles(): List<BackupFileInfo> {
        val allFiles = mutableListOf<BackupFileInfo>()

        // Add internal backup files
        val internalFiles = getInternalBackupDirectory().listFiles { file ->
            file.isFile && file.name.endsWith(BACKUP_EXTENSION)
        }?.map { file ->
            BackupFileInfo(
                name = file.name,
                path = file.absolutePath,
                size = file.length(),
                lastModified = file.lastModified(),
                isExternal = false
            )
        } ?: emptyList()

        allFiles.addAll(internalFiles)

        // Add external backup files
        val externalDir = getExternalBackupDirectory()
        if (externalDir != null && externalDir.exists()) {
            val externalFiles = externalDir.listFiles { file ->
                file.isFile && file.name.endsWith(BACKUP_EXTENSION)
            }?.map { file ->
                BackupFileInfo(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified(),
                    isExternal = true
                )
            } ?: emptyList()

            allFiles.addAll(externalFiles)
        }

        // Remove duplicates and sort by date
        return allFiles
            .distinctBy { it.name }
            .sortedByDescending { it.lastModified }
    }

    /**
     * Lists all backup files in the backup directory
     */
    fun listBackupFiles(): List<BackupFileInfo> {
        return listAllBackupFiles()
    }

    /**
     * Deletes a backup file
     */
    fun deleteBackupFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            val deleted = file.delete()
            if (deleted) {
                Log.i(TAG, "Backup file deleted: $filePath")
            } else {
                Log.w(TAG, "Failed to delete backup file: $filePath")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup file", e)
            false
        }
    }

    /**
     * Gets the backup directory path for user information
     */
    fun getBackupDirectoryPath(): String {
        val externalDir = getExternalBackupDirectory()
        return externalDir?.absolutePath ?: "Internal storage only"
    }

    /**
     * Checks if external storage is available
     */
    fun isExternalStorageAvailable(): Boolean {
        return getExternalBackupDirectory() != null
    }

    /**
     * Cleans up old backup files, keeping only the specified number
     */
    fun cleanupOldBackups(keepCount: Int = 10) {
        val backupFiles = listBackupFiles()

        if (backupFiles.size > keepCount) {
            val filesToDelete = backupFiles.drop(keepCount)

            filesToDelete.forEach { fileInfo ->
                if (deleteBackupFile(fileInfo.path)) {
                    Log.i(TAG, "Cleaned up old backup: ${fileInfo.name}")
                }
            }
        }
    }
}

/**
 * Result of backup save operation
 */
data class BackupSaveResult(
    val internalPath: String,
    val externalPath: String?,
    val fileName: String,
    val isExternalAvailable: Boolean
)

/**
 * Enhanced backup file information
 */
data class BackupFileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isExternal: Boolean = false
) {
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(lastModified))
    }

    fun getLocationDescription(): String {
        return if (isExternal) "Downloads/SecureVault_Backups" else "Internal Storage"
    }
}

/**
 * Custom exception for file operations
 */
class FileOperationException(message: String, cause: Throwable? = null) : Exception(message, cause)