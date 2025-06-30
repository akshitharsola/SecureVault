// app/src/main/java/com/securevault/utils/ClipboardManager.kt
package com.securevault.utils

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages clipboard operations with security features
 */
class ClipboardManager(private val context: Context) {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager

    companion object {
        private const val TAG = "ClipboardManager"
        private const val AUTO_CLEAR_DELAY = 60000L // 60 seconds
    }

    /**
     * Copies text to clipboard with auto-clear functionality
     * @param label Label for the clipboard entry
     * @param text Text to copy
     * @param autoClear Whether to automatically clear clipboard after delay
     */
    fun copyToClipboard(label: String, text: String, autoClear: Boolean = true) {
        try {
            val clip = ClipData.newPlainText(label, text)
            clipboardManager.setPrimaryClip(clip)

            Log.d(TAG, "Copied '$label' to clipboard")

            if (autoClear) {
                scheduleAutoClear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying to clipboard: ${e.message}")
        }
    }

    /**
     * Copies username and password together
     * @param username Username to copy
     * @param password Password to copy
     */
    fun copyCredentials(username: String, password: String) {
        val credentials = "$username\n$password"
        copyToClipboard("Credentials", credentials, true)
    }

    /**
     * Clears the clipboard
     */
    fun clearClipboard() {
        try {
            val clip = ClipData.newPlainText("", "")
            clipboardManager.setPrimaryClip(clip)
            Log.d(TAG, "Clipboard cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing clipboard: ${e.message}")
        }
    }

    /**
     * Schedules automatic clipboard clearing
     */
    private fun scheduleAutoClear() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(AUTO_CLEAR_DELAY)
            clearClipboard()
        }
    }

    /**
     * Gets current clipboard content (for debugging)
     */
    fun getClipboardContent(): String? {
        return try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading clipboard: ${e.message}")
            null
        }
    }
}