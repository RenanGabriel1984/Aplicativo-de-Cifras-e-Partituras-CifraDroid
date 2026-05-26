package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Manuscript::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun manuscriptDao(): ManuscriptDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "manuscript_database")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
