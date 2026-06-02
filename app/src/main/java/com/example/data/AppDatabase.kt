package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Manuscript::class, Repertoire::class, TranspositionPreference::class, PdfTextContent::class, SongChart::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun manuscriptDao(): ManuscriptDao
    abstract fun repertoireDao(): RepertoireDao
    abstract fun transpositionDao(): TranspositionDao
    abstract fun pdfTextContentDao(): PdfTextContentDao
    abstract fun songChartDao(): SongChartDao

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
        
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `song_charts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `manuscriptId` INTEGER NOT NULL, `title` TEXT NOT NULL, `originalKey` TEXT NOT NULL, `content` TEXT NOT NULL, `savedKey` TEXT, FOREIGN KEY(`manuscriptId`) REFERENCES `manuscripts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_song_charts_manuscriptId` ON `song_charts` (`manuscriptId`)")
            }
        }

        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `song_charts` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("DROP INDEX IF EXISTS `index_song_charts_manuscriptId`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_song_charts_manuscriptId` ON `song_charts` (`manuscriptId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "manuscript_database")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
