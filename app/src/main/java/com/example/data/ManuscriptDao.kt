package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ManuscriptDao {
    @Query("SELECT * FROM manuscripts ORDER BY lastUsedTimestamp DESC")
    fun getAllManuscripts(): Flow<List<Manuscript>>

    @Query("SELECT * FROM manuscripts WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<Manuscript>>

    @Query("SELECT * FROM manuscripts WHERE category = :category ORDER BY title ASC")
    fun getByCategory(category: String): Flow<List<Manuscript>>
    
    @Query("SELECT * FROM manuscripts WHERE title LIKE '%' || :query || '%' OR composer LIKE '%' || :query || '%'")
    fun searchManuscripts(query: String): Flow<List<Manuscript>>

    @Query("SELECT * FROM manuscripts WHERE id = :id LIMIT 1")
    fun getManuscriptById(id: Int): Flow<Manuscript>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(manuscripts: List<Manuscript>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManuscript(manuscript: Manuscript)

    @Update
    suspend fun updateManuscript(manuscript: Manuscript)

    @Delete
    suspend fun deleteManuscript(manuscript: Manuscript)
}
