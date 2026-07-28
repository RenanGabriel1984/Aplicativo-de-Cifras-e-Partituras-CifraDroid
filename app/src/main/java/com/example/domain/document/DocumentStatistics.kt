package com.example.domain.document

data class DocumentStatistics(
    val totalSections: Int,
    val totalLyricsLines: Int,
    val totalChordLines: Int,
    val estimatedDuration: Long,
    val difficulty: String
)
