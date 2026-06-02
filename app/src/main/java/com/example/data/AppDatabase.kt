package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Manuscript::class, Repertoire::class, TranspositionPreference::class, PdfTextContent::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun manuscriptDao(): ManuscriptDao
    abstract fun repertoireDao(): RepertoireDao
    abstract fun transpositionDao(): TranspositionDao
    abstract fun pdfTextContentDao(): PdfTextContentDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `transposition_preferences` (`manuscriptId` INTEGER NOT NULL, `preferredKey` TEXT NOT NULL, PRIMARY KEY(`manuscriptId`))")
            }
        }

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `pdf_text_content` (`manuscriptId` INTEGER NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`manuscriptId`))")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "manuscript_database")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
