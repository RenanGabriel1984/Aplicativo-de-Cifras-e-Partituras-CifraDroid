package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongChartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(songChart: SongChart)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songCharts: List<SongChart>)

    @Query("SELECT * FROM song_charts WHERE manuscriptId = :manuscriptId ORDER BY sortOrder ASC")
    fun getByManuscriptId(manuscriptId: Int): Flow<List<SongChart>>

    @Query("SELECT * FROM song_charts ORDER BY title ASC")
    fun getAllSongCharts(): Flow<List<SongChart>>
    
    @Query("SELECT * FROM song_charts WHERE id = :songChartId")
    fun getById(songChartId: Int): Flow<SongChart?>

    @Query("DELETE FROM song_charts")
    suspend fun deleteAll()

    @Query("UPDATE song_charts SET savedKey = :newKey WHERE id = :songChartId")
    suspend fun updateSavedKey(songChartId: Int, newKey: String?)
}
