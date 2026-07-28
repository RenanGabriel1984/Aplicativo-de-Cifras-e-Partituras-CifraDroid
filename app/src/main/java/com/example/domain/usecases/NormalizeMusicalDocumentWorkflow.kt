package com.example.domain.usecases

import com.example.domain.document.MusicalDocument
import com.example.domain.analysis.MusicalAnalysisEngine
import com.example.domain.analysis.MusicalAnalysisStatistics
import com.example.domain.editor.MusicalStructureEditor

object NormalizeMusicalDocumentWorkflow {

    fun normalize(document: MusicalDocument): NormalizeResult {
        val withNormalizedMetadata = normalizeMetadata(document)
        val withNormalizedSections = normalizeSections(withNormalizedMetadata)
        val withNormalizedStructure = normalizeStructure(withNormalizedSections)
        
        val stats = normalizeStatistics(withNormalizedStructure)
        
        val isModified = document != withNormalizedStructure
        
        return NormalizeResult(
            document = withNormalizedStructure,
            statistics = stats,
            isModified = isModified
        )
    }

    fun normalizeMetadata(document: MusicalDocument): MusicalDocument {
        // Garantir títulos válidos
        val safeTitle = document.metadata.title.trim().takeIf { it.isNotEmpty() } ?: "Untitled"
        return document.copy(metadata = document.metadata.copy(title = safeTitle))
    }

    fun normalizeSections(document: MusicalDocument): MusicalDocument {
        var currentDoc = document
        currentDoc.sections.forEach { section ->
            val safeTitle = if (section.title.isBlank()) section.semanticType else section.title.trim()
            if (safeTitle != section.title) {
                currentDoc = MusicalStructureEditor.renameSection(currentDoc, section.id, safeTitle)
            }
        }
        return currentDoc
    }

    fun normalizeStructure(document: MusicalDocument): MusicalDocument {
        // Garantir ordem correta das seções (reordenar e reatribuir índices sequenciais)
        val sortedSections = document.sections.sortedBy { it.order }.mapIndexed { index, section ->
            section.copy(order = index)
        }
        return document.copy(sections = sortedSections)
    }

    fun normalizeStatistics(document: MusicalDocument): MusicalAnalysisStatistics {
        // Estatísticas recalculadas usando o AnalysisEngine
        return MusicalAnalysisEngine.countSections(document)
    }
}
