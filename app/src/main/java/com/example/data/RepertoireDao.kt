package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RepertoireDao {
    @Query("SELECT * FROM repertoires")
    fun getAllRepertoires(): Flow<List<Repertoire>>

    @Query("SELECT * FROM repertoires WHERE id = :id")
    fun getRepertoireById(id: Int): Flow<Repertoire>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repertoire: Repertoire): Long
    
    @Query("DELETE FROM repertoires WHERE id = :id")
    suspend fun delete(id: Int)
}
