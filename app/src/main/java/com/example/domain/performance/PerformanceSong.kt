package com.example.domain.performance

import com.example.domain.document.MusicalDocument

data class PerformanceSong(
    val id: String,
    val songDocument: MusicalDocument,
    val status: PerformanceStatus,
    val notes: String,
    val lastReviewed: Long
)
