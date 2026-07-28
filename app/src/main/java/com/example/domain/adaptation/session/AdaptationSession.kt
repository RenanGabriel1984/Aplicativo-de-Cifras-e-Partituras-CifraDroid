package com.example.domain.adaptation.session

import com.example.domain.adaptation.MusicalAdaptation

data class AdaptationSession(
    val sessionId: String,
    val sourceAdaptation: MusicalAdaptation,
    val currentRequest: AdaptationRequest?,
    val preview: AdaptationPreview?,
    val isConfirmed: Boolean
)
