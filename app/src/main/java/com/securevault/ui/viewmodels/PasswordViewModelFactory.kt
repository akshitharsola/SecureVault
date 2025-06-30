// app/src/main/java/com/example/securevault/ui/viewmodels/PasswordViewModelFactory.kt
package com.securevault.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.securevault.ui.screens.detail.DetailViewModel
import com.securevault.ui.screens.form.FormViewModel
import com.securevault.ui.screens.main.MainViewModel
import com.securevault.ui.screens.settings.SettingsViewModel

class PasswordViewModelFactory(
    private val application: Application,
    private val passwordId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(application) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(application, passwordId ?: "") as T
            }
            modelClass.isAssignableFrom(FormViewModel::class.java) -> {
                FormViewModel(application, passwordId) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}