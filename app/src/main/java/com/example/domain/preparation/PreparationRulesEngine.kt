package com.example.domain.preparation

import com.example.domain.document.MusicalDocument

object PreparationRulesEngine {

    fun evaluate(document: MusicalDocument): List<PreparationHint> {
        return generateHints(document)
    }

    fun generateHints(document: MusicalDocument): List<PreparationHint> {
        val hints = mutableListOf<PreparationHint>()

        if (document.metadata.bpm == 0) {
            hints.add(
                PreparationHint(
                    id = "missing_bpm",
                    title = "BPM Ausente",
                    description = "Adicionar BPM facilita o estudo.",
                    severity = PreparationSeverity.SUGGESTION
                )
            )
        }

        if (document.metadata.tags.isEmpty()) {
            hints.add(
                PreparationHint(
                    id = "missing_notes",
                    title = "Sem observações",
                    description = "Considere adicionar observações.",
                    severity = PreparationSeverity.INFO
                )
            )
        }

        if (document.metadata.category.name == "GENERAL") {
            hints.add(
                PreparationHint(
                    id = "missing_category",
                    title = "Categoria Indefinida",
                    description = "Defina a categoria da música.",
                    severity = PreparationSeverity.IMPORTANT
                )
            )
        }

        if (isPerformanceReady(document)) {
            hints.add(
                PreparationHint(
                    id = "performance_ready",
                    title = "Pronta",
                    description = "Música pronta para apresentação.",
                    severity = PreparationSeverity.READY
                )
            )
        }

        return hints
    }

    fun isPerformanceReady(document: MusicalDocument): Boolean {
        return MusicalPreparationEngine.estimatedPerformanceReady(document)
    }
}
