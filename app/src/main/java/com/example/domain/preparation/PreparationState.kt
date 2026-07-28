package com.example.domain.preparation

import com.example.domain.document.MusicalDocument

data class PreparationState(
    val document: MusicalDocument,
    val availableActions: List<PreparationAction>,
    val destinations: List<PreparationDestination>,
    val summary: PreparationSummary
)
