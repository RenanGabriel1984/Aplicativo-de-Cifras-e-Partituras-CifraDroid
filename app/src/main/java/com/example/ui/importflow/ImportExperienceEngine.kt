package com.example.ui.importflow

import com.example.domain.document.MusicalDocument
import com.example.domain.usecases.ImportMusicWorkflow
import com.example.domain.usecases.NormalizeMusicalDocumentWorkflow

object ImportExperienceEngine {

    fun startImport(document: MusicalDocument): ImportExperienceState {
        val normalizeResult = NormalizeMusicalDocumentWorkflow.normalize(document)
        val importResult = ImportMusicWorkflow.execute(normalizeResult.document)
        
        return ImportExperienceState(
            documentId = document.id,
            importResult = importResult,
            normalizeResult = normalizeResult,
            summary = null,
            destination = null,
            isConfirmed = false
        )
    }

    fun buildSummary(state: ImportExperienceState): ImportExperienceState {
        val importResult = state.importResult ?: return state
        
        val summary = ImportSummary(
            title = importResult.identity.title,
            artist = importResult.identity.artist,
            key = importResult.identity.originalKey,
            category = importResult.identity.category,
            origin = importResult.identity.versions.firstOrNull()?.origin?.source?.name ?: "UNKNOWN",
            summaryText = "${importResult.identity.title} possui ${importResult.analysis.statistics.totalSections} seções identificadas e tem duração estimada de ${importResult.analysis.estimatedDurationSeconds} segundos."
        )
        return state.copy(summary = summary)
    }

    fun suggestDestination(state: ImportExperienceState): ImportExperienceState {
        val category = state.importResult?.identity?.category?.uppercase() ?: ""
        
        val dest = ImportDestination(
            suggestedWorkspace = "Biblioteca Principal",
            suggestedSessionId = if (category == "LITURGICAL" || category == "WORSHIP") "Próxima Missa/Culto" else "Ensaio"
        )
        
        return state.copy(destination = dest)
    }

    fun confirmImport(state: ImportExperienceState): ImportExperienceState {
        return state.copy(isConfirmed = true)
    }
}
