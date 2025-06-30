// app/src/main/java/com/example/securevault/utils/SecurityManager.kt
package com.securevault.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecurityManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val KEY_SIZE = 256
    }

    private fun getOrCreateKey(): SecretKey {
        var encodedKey = sharedPreferences.getString(KEY_ENCRYPTION_KEY, null)

        if (encodedKey == null) {
            // Generate a new key
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
            keyGenerator.init(KEY_SIZE, SecureRandom())
            val key = keyGenerator.generateKey()

            // Save the key
            encodedKey = Base64.encodeToString(key.encoded, Base64.DEFAULT)
            sharedPreferences.edit().putString(KEY_ENCRYPTION_KEY, encodedKey).apply()

            return key
        } else {
            // Restore existing key
            val keyBytes = Base64.decode(encodedKey, Base64.DEFAULT)
            return SecretKeySpec(keyBytes, ALGORITHM)
        }
    }

    fun encrypt(text: String): String {
        val key = getOrCreateKey()

        // Generate random IV
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        // Encrypt
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encrypted = cipher.doFinal(text.toByteArray())

        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        val key = getOrCreateKey()

        // Decode from Base64
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)

        // Extract IV and encrypted data
        val iv = ByteArray(16)
        val encrypted = ByteArray(combined.size - iv.size)
        System.arraycopy(combined, 0, iv, 0, iv.size)
        System.arraycopy(combined, iv.size, encrypted, 0, encrypted.size)

        // Decrypt
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        return String(cipher.doFinal(encrypted))
    }
}