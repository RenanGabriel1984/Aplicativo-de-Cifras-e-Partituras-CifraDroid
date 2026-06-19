package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RepertoireCategoryDao {
    @Query("SELECT * FROM repertoire_categories WHERE repertoireId = :repertoireId ORDER BY position ASC")
    fun getCategoriesForRepertoire(repertoireId: Int): Flow<List<RepertoireCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: RepertoireCategory): Long

    @Query("SELECT * FROM repertoire_categories")
    fun getAll(): Flow<List<RepertoireCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<RepertoireCategory>)

    @Query("DELETE FROM repertoire_categories")
    suspend fun deleteAll()

    @Query("DELETE FROM repertoire_categories WHERE repertoireId = :repertoireId")
    suspend fun deleteCategoriesForRepertoire(repertoireId: Int)
}
