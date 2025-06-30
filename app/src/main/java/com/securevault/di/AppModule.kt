// app/src/main/java/com/securevault/di/AppModule.kt
package com.securevault.di

import android.content.Context
import androidx.room.Room
import com.securevault.data.local.PasswordDatabase
import com.securevault.data.repository.PasswordRepository
import com.securevault.data.repository.PasswordRepositoryImpl
import com.securevault.domain.usecase.*
import com.securevault.utils.BackupManager
import com.securevault.utils.BiometricHelper
import com.securevault.utils.ClipboardManager
import com.securevault.utils.SecurityManager
import com.securevault.utils.ThemeManager

object AppModule {

    @Volatile
    private var database: PasswordDatabase? = null

    @Volatile
    private var repository: PasswordRepository? = null

    @Volatile
    private var backupManager: BackupManager? = null

    fun providePasswordDatabase(context: Context): PasswordDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                PasswordDatabase::class.java,
                "password_database"
            ).build().also { database = it }
        }
    }

    fun providePasswordRepository(context: Context): PasswordRepository {
        return repository ?: synchronized(this) {
            repository ?: PasswordRepositoryImpl(
                providePasswordDatabase(context).passwordDao()
            ).also { repository = it }
        }
    }

    fun provideBiometricHelper(context: Context): BiometricHelper {
        return BiometricHelper(context)
    }

    fun provideSecurityManager(context: Context): SecurityManager {
        return SecurityManager(context)
    }

    fun provideThemeManager(context: Context): ThemeManager {
        return ThemeManager(context)
    }

    fun provideClipboardManager(context: Context): ClipboardManager {
        return ClipboardManager(context)
    }

    // Backup-related dependencies
    fun provideBackupManager(context: Context): BackupManager {
        return backupManager ?: synchronized(this) {
            backupManager ?: BackupManager(
                context = context,
                passwordRepository = providePasswordRepository(context)
            ).also { backupManager = it }
        }
    }

    // Use cases
    fun provideGetPasswordsUseCase(context: Context): GetPasswordsUseCase {
        return GetPasswordsUseCase(providePasswordRepository(context))
    }

    fun provideGetPasswordUseCase(context: Context): GetPasswordUseCase {
        return GetPasswordUseCase(providePasswordRepository(context))
    }

    fun provideSavePasswordUseCase(context: Context): SavePasswordUseCase {
        return SavePasswordUseCase(providePasswordRepository(context))
    }

    fun provideDeletePasswordUseCase(context: Context): DeletePasswordUseCase {
        return DeletePasswordUseCase(providePasswordRepository(context))
    }

    fun provideDeleteAllPasswordsUseCase(context: Context): DeleteAllPasswordsUseCase {
        return DeleteAllPasswordsUseCase(providePasswordRepository(context))
    }

    fun provideSearchPasswordsUseCase(context: Context): SearchPasswordsUseCase {
        return SearchPasswordsUseCase(providePasswordRepository(context))
    }

    // Backup use cases
    fun provideCreateBackupUseCase(context: Context): CreateBackupUseCase {
        return CreateBackupUseCase(provideBackupManager(context))
    }

    fun provideRestoreBackupUseCase(context: Context): RestoreBackupUseCase {
        return RestoreBackupUseCase(provideBackupManager(context))
    }

    fun provideValidateBackupFileUseCase(context: Context): ValidateBackupFileUseCase {
        return ValidateBackupFileUseCase(provideBackupManager(context))
    }
}