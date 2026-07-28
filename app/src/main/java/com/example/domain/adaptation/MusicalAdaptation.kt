package com.example.domain.adaptation

import com.example.domain.document.MusicalDocument

data class MusicalAdaptation(
    val id: String,
    val identityId: String,
    val originalDocumentId: String,
    val profile: AdaptationProfile,
    val adaptedDocument: MusicalDocument,
    val history: AdaptationHistory
)
