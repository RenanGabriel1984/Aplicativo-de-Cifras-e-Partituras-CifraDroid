package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RepertoireSongDao {
    @Query("SELECT * FROM repertoire_songs WHERE repertoireId = :repertoireId ORDER BY position ASC")
    fun getSongsForRepertoire(repertoireId: Int): Flow<List<RepertoireSong>>

    @Query("SELECT * FROM repertoire_songs WHERE repertoireId = :repertoireId AND songChartId = :songChartId LIMIT 1")
    fun findRepertoireSong(repertoireId: Int, songChartId: Int): Flow<RepertoireSong?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: RepertoireSong): Long

    @Query("DELETE FROM repertoire_songs WHERE repertoireId = :repertoireId")
    suspend fun deleteSongsForRepertoire(repertoireId: Int)

    @Query("SELECT * FROM repertoire_songs WHERE repertoireId = :repertoireId")
    suspend fun getSongsForRepertoireSync(repertoireId: Int): List<RepertoireSong>

    @Query("UPDATE repertoire_songs SET customKey = :newKey WHERE id = :repertoireSongId")
    suspend fun updateCustomKey(repertoireSongId: Int, newKey: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<RepertoireSong>)

    @Query("SELECT * FROM repertoire_songs")
    fun getAll(): Flow<List<RepertoireSong>>

    @Query("DELETE FROM repertoire_songs")
    suspend fun deleteAll()

    @Query("SELECT * FROM repertoire_songs WHERE id = :id")
    fun getById(id: Int): Flow<RepertoireSong?>
}
