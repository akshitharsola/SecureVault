// app/src/main/java/com/securevault/utils/BackupEncryption.kt
package com.securevault.utils

import android.util.Base64
import android.util.Log
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encryption utilities for backup files
 * Compatible with iOS implementation using PBKDF2 + AES-256-CBC
 */
class BackupEncryption {

    companion object {
        private const val TAG = "BackupEncryption"
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_LENGTH = 256
        private const val IV_LENGTH = 16
        private const val SALT_LENGTH = 32
        private const val ITERATION_COUNT = 100000 // Strong iteration count for PBKDF2
    }

    /**
     * Encrypts data using password-based encryption
     * @param data The data to encrypt
     * @param password The password for encryption
     * @return Base64 encoded encrypted data with salt and IV prepended
     */
    fun encrypt(data: String, password: String): String {
        try {
            // Generate random salt and IV
            val salt = ByteArray(SALT_LENGTH)
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(salt)
            SecureRandom().nextBytes(iv)

            // Derive key from password using PBKDF2
            val key = deriveKey(password, salt)

            // Encrypt the data
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            // Combine salt + IV + encrypted data
            val combined = ByteArray(SALT_LENGTH + IV_LENGTH + encryptedData.size)
            System.arraycopy(salt, 0, combined, 0, SALT_LENGTH)
            System.arraycopy(iv, 0, combined, SALT_LENGTH, IV_LENGTH)
            System.arraycopy(encryptedData, 0, combined, SALT_LENGTH + IV_LENGTH, encryptedData.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data", e)
            throw EncryptionException("Failed to encrypt data: ${e.message}", e)
        }
    }

    /**
     * Decrypts data using password-based encryption
     * @param encryptedData Base64 encoded encrypted data with salt and IV
     * @param password The password for decryption
     * @return Decrypted data as string
     */
    fun decrypt(encryptedData: String, password: String): String {
        try {
            // Decode from Base64
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

            if (combined.size < SALT_LENGTH + IV_LENGTH) {
                throw DecryptionException("Invalid encrypted data format")
            }

            // Extract salt, IV, and encrypted data
            val salt = ByteArray(SALT_LENGTH)
            val iv = ByteArray(IV_LENGTH)
            val encrypted = ByteArray(combined.size - SALT_LENGTH - IV_LENGTH)

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH)
            System.arraycopy(combined, SALT_LENGTH, iv, 0, IV_LENGTH)
            System.arraycopy(combined, SALT_LENGTH + IV_LENGTH, encrypted, 0, encrypted.size)

            // Derive key from password using same salt
            val key = deriveKey(password, salt)

            // Decrypt the data
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            val decryptedData = cipher.doFinal(encrypted)

            return String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data", e)
            when (e) {
                is DecryptionException -> throw e
                else -> throw DecryptionException("Failed to decrypt data: ${e.message}", e)
            }
        }
    }

    /**
     * Derives encryption key from password using PBKDF2
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val key = factory.generateSecret(spec)
        return SecretKeySpec(key.encoded, ALGORITHM)
    }

    /**
     * Validates if the provided password can decrypt the backup data
     * This is useful for password verification before attempting full restore
     */
    fun validatePassword(encryptedData: String, password: String): Boolean {
        return try {
            // Try to decrypt just a small portion to validate password
            decrypt(encryptedData, password)
            true
        } catch (e: Exception) {
            Log.d(TAG, "Password validation failed: ${e.message}")
            false
        }
    }
}

/**
 * Custom exceptions for encryption/decryption operations
 */
class EncryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)
class DecryptionException(message: String, cause: Throwable? = null) : Exception(message, cause)