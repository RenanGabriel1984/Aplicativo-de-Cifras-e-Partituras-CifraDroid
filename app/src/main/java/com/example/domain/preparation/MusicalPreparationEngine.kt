package com.example.domain.preparation

import com.example.domain.document.MusicalDocument

object MusicalPreparationEngine {

    fun createPreparation(document: MusicalDocument): PreparationState {
        return createPreparationState(document)
    }

    fun createPreparationState(document: MusicalDocument): PreparationState {
        return PreparationState(
            document = document,
            availableActions = availableActions(),
            destinations = availableDestinations(),
            summary = calculateSummary(document)
        )
    }

    fun isReady(document: MusicalDocument): Boolean {
        return document.metadata.title.isNotBlank() &&
               document.metadata.artist.isNotBlank() &&
               document.metadata.key.isNotBlank() &&
               document.metadata.category.name.isNotBlank() // since it's an enum, we just check something or just assume it is
    }

    fun missingFields(document: MusicalDocument): Int {
        var count = 0
        if (document.metadata.title.isBlank()) count++
        if (document.metadata.artist.isBlank()) count++
        if (document.metadata.key.isBlank()) count++
        // Since category is an enum, it's always populated. But let's add logic in case.
        return count
    }

    fun estimatedPerformanceReady(document: MusicalDocument): Boolean {
        return isReady(document) && document.sections.isNotEmpty()
    }

    fun calculateSummary(document: MusicalDocument): PreparationSummary {
        return PreparationSummary(
            ready = isReady(document),
            missingFields = missingFields(document),
            estimatedPerformanceReady = estimatedPerformanceReady(document)
        )
    }

    fun availableActions(): List<PreparationAction> {
        return listOf(
            PreparationAction.CHANGE_KEY,
            PreparationAction.CHANGE_CAPO,
            PreparationAction.CHANGE_BPM,
            PreparationAction.ADD_NOTE,
            PreparationAction.MARK_ENTRY,
            PreparationAction.MARK_END,
            PreparationAction.MARK_REPEAT,
            PreparationAction.MARK_SOLO,
            PreparationAction.MARK_DYNAMICS,
            PreparationAction.MARK_BREATH
        )
    }

    fun availableDestinations(): List<PreparationDestination> {
        return listOf(
            PreparationDestination.WORKSPACE,
            PreparationDestination.REPERTOIRE,
            PreparationDestination.FAVORITES,
            PreparationDestination.MASS,
            PreparationDestination.REHEARSAL,
            PreparationDestination.RETREAT,
            PreparationDestination.CUSTOM
        )
    }
}
