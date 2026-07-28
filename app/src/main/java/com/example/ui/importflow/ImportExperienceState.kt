package com.example.ui.importflow

import com.example.domain.usecases.ImportMusicResult
import com.example.domain.usecases.NormalizeResult

data class ImportExperienceState(
    val documentId: String,
    val importResult: ImportMusicResult?,
    val normalizeResult: NormalizeResult?,
    val summary: ImportSummary?,
    val destination: ImportDestination?,
    val isConfirmed: Boolean
)
