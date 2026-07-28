package com.example.domain.usecases

import com.example.domain.document.MusicalDocument
import com.example.domain.analysis.MusicalAnalysisStatistics

data class NormalizeResult(
    val document: MusicalDocument,
    val statistics: MusicalAnalysisStatistics,
    val isModified: Boolean
)
