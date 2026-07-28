package com.example.util

object ConductorEngine {
    fun evaluate(
        musicalStructure: MusicalStructure,
        musicalSemantics: List<MusicalSemanticBlock>,
        timeline: MusicalTimeline,
        currentPage: Int
    ): ConductorState {
        val sections = musicalSemantics.map { block ->
            val isActive = currentPage in block.startPage..block.endPage
            val isCompleted = currentPage > block.endPage
            ConductorSection(
                id = block.id,
                type = block.type,
                startPage = block.startPage,
                endPage = block.endPage,
                active = isActive,
                completed = isCompleted
            )
        }

        val currentSectionIndex = sections.indexOfFirst { it.active }.takeIf { it >= 0 } ?: 0
        
        val firstPage = sections.minOfOrNull { it.startPage } ?: 1
        val lastPage = sections.maxOfOrNull { it.endPage } ?: 1
        val totalSpan = (lastPage - firstPage).coerceAtLeast(1).toFloat()
        val currentSpan = (currentPage - firstPage).coerceAtLeast(0).toFloat()
        val progress = (currentSpan / totalSpan).coerceIn(0f, 1f)

        return ConductorState(
            sections = sections,
            currentSection = currentSectionIndex,
            progress = progress
        )
    }
}
