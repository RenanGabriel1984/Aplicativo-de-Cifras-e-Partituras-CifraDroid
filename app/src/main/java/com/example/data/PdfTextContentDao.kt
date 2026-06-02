package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfTextContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePdfText(content: PdfTextContent)

    @Query("SELECT * FROM pdf_text_content WHERE manuscriptId = :id")
    fun getPdfText(id: Int): Flow<PdfTextContent?>

    @Query("DELETE FROM pdf_text_content WHERE manuscriptId = :id")
    suspend fun deletePdfText(id: Int)
}
