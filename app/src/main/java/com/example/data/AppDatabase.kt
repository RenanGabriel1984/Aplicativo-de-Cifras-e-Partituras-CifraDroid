package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [
    Manuscript::class, 
    Repertoire::class, 
    TranspositionPreference::class, 
    PdfTextContent::class, 
    SongChart::class,
    RepertoireCategory::class,
    RepertoireSong::class
], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun manuscriptDao(): ManuscriptDao
    abstract fun repertoireDao(): RepertoireDao
    abstract fun transpositionDao(): TranspositionDao
    abstract fun pdfTextContentDao(): PdfTextContentDao
    abstract fun songChartDao(): SongChartDao
    abstract fun repertoireCategoryDao(): RepertoireCategoryDao
    abstract fun repertoireSongDao(): RepertoireSongDao

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

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create RepertoireCategory table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `repertoire_categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `repertoireId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `position` INTEGER NOT NULL,
                        FOREIGN KEY(`repertoireId`) REFERENCES `repertoires`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_repertoire_categories_repertoireId` ON `repertoire_categories` (`repertoireId`)")

                // Create RepertoireSong table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `repertoire_songs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `repertoireId` INTEGER NOT NULL,
                        `categoryId` INTEGER,
                        `songChartId` INTEGER NOT NULL,
                        `position` INTEGER NOT NULL,
                        `customKey` TEXT,
                        FOREIGN KEY(`repertoireId`) REFERENCES `repertoires`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`categoryId`) REFERENCES `repertoire_categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`songChartId`) REFERENCES `song_charts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_repertoire_songs_repertoireId` ON `repertoire_songs` (`repertoireId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_repertoire_songs_categoryId` ON `repertoire_songs` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_repertoire_songs_songChartId` ON `repertoire_songs` (`songChartId`)")

                // Migrate data from repertoire.manuscriptIdsJson
                val cursor = db.query("SELECT id, manuscriptIdsJson FROM repertoires")
                while (cursor.moveToNext()) {
                    val repId = cursor.getInt(0)
                    val json = cursor.getString(1)?.trim() ?: ""

                    if (json.startsWith("{")) {
                        try {
                            val obj = org.json.JSONObject(json)
                            val categoriesArr = obj.optJSONArray("categories") ?: org.json.JSONArray()
                            var globalSongPosition = 0
                            for (i in 0 until categoriesArr.length()) {
                                val catObj = categoriesArr.optJSONObject(i) ?: continue
                                val catName = catObj.optString("name", "Sem Categoria")
                                
                                val catInsertId = db.insert(
                                    "repertoire_categories",
                                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                                    android.content.ContentValues().apply {
                                        put("repertoireId", repId)
                                        put("name", catName)
                                        put("position", i)
                                    }
                                )

                                val idsArr = catObj.optJSONArray("items") ?: org.json.JSONArray()
                                for (j in 0 until idsArr.length()) {
                                    val songId = idsArr.getInt(j)
                                    db.insert(
                                        "repertoire_songs",
                                        android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                                        android.content.ContentValues().apply {
                                            put("repertoireId", repId)
                                            put("categoryId", catInsertId)
                                            put("songChartId", songId)
                                            put("position", globalSongPosition++)
                                        }
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MIGRATION_9_10", "Error parsing categories json for rep $repId: $json", e)
                        }
                    } else if (json.startsWith("[")) {
                        try {
                            val arr = org.json.JSONArray(json)
                            
                            val defaultCatId = db.insert(
                                "repertoire_categories",
                                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                                android.content.ContentValues().apply {
                                    put("repertoireId", repId)
                                    put("name", "Principal")
                                    put("position", 0)
                                }
                            )

                            for (i in 0 until arr.length()) {
                                val songId = arr.getInt(i)
                                db.insert(
                                    "repertoire_songs",
                                    android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE,
                                    android.content.ContentValues().apply {
                                        put("repertoireId", repId)
                                        put("categoryId", defaultCatId)
                                        put("songChartId", songId)
                                        put("position", i)
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MIGRATION_9_10", "Error parsing array json for rep $repId: $json", e)
                        }
                    }
                }
                cursor.close()
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "manuscript_database")
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
