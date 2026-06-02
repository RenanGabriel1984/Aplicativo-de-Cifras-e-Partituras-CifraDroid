package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranspositionDao {
    @Query("SELECT * FROM transposition_preferences WHERE manuscriptId = :id")
    fun getPreference(id: Int): Flow<TranspositionPreference?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreference(preference: TranspositionPreference)
}
