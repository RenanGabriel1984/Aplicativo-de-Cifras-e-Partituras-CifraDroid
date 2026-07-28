package com.example.domain.usecases

import com.example.domain.identity.MusicalIdentity
import com.example.domain.document.MusicalDocument
import com.example.domain.analysis.MusicalAnalysis

data class ImportMusicResult(
    val identity: MusicalIdentity,
    val songDocument: MusicalDocument,
    val analysis: MusicalAnalysis,
    val preparationHints: Any, // Placeholder for PreparationHints as we don't have the exact type
    val advisor: Any // Placeholder for PreparationAdvisor
)
