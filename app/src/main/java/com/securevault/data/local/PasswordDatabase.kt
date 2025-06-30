// app/src/main/java/com/securevault/data/local/PasswordDatabase.kt
package com.securevault.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PasswordEntity::class], version = 1, exportSchema = false)
abstract class PasswordDatabase : RoomDatabase() {

    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: PasswordDatabase? = null

        fun getInstance(context: Context): PasswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PasswordDatabase::class.java,
                    "password_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}