package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_text_content")
data class PdfTextContent(
    @PrimaryKey
    val manuscriptId: Int,
    val content: String
)
