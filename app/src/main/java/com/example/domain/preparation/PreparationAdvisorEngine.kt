package com.example.domain.preparation

object PreparationAdvisorEngine {

    fun generateAdvice(hints: List<PreparationHint>): List<PreparationAdvice> {
        val advices = hints.map { createAdvice(it) }
        return sortAdvice(advices)
    }

    fun createAdvice(hint: PreparationHint): PreparationAdvice {
        return when (hint.id) {
            "missing_bpm" -> PreparationAdvice(
                title = hint.title,
                message = "Adicionar o BPM permitirá utilizar recursos avançados de estudo.",
                priority = 2,
                actionSuggested = PreparationAction.CHANGE_BPM
            )
            "missing_category" -> PreparationAdvice(
                title = hint.title,
                message = "Organize esta música para encontrá-la rapidamente.",
                priority = 3,
                actionSuggested = null
            )
            "performance_ready" -> PreparationAdvice(
                title = hint.title,
                message = "Excelente! Sua música está pronta para apresentação.",
                priority = 4,
                actionSuggested = null
            )
            "missing_notes" -> PreparationAdvice(
                title = hint.title,
                message = "Considere adicionar observações.",
                priority = 1,
                actionSuggested = PreparationAction.ADD_NOTE
            )
            else -> PreparationAdvice(
                title = hint.title,
                message = hint.description,
                priority = 0,
                actionSuggested = null
            )
        }
    }

    fun sortAdvice(advices: List<PreparationAdvice>): List<PreparationAdvice> {
        return advices.sortedByDescending { it.priority }
    }
}
