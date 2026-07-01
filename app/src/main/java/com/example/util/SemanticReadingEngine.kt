package com.example.util

object SemanticReadingEngine {
    fun produceState(
        currentPage: Int,
        semantics: List<MusicalSemanticBlock>
    ): SemanticReadingState {
        val currentBlock = semantics.firstOrNull { currentPage in it.startPage..it.endPage }
        val currentSemantic = currentBlock?.type ?: MusicalSemanticType.UNKNOWN

        return when (currentSemantic) {
            MusicalSemanticType.INTRO -> SemanticReadingState(currentSemantic, "Introdução", "Prepare-se", PresentationPriority.LOW)
            MusicalSemanticType.VERSE -> SemanticReadingState(currentSemantic, "Verso", "Narrativa principal", PresentationPriority.LOW)
            MusicalSemanticType.CHORUS -> SemanticReadingState(currentSemantic, "Refrão", "Trecho principal", PresentationPriority.MEDIUM)
            MusicalSemanticType.BRIDGE -> SemanticReadingState(currentSemantic, "Ponte", "Mudança harmônica", PresentationPriority.MEDIUM)
            MusicalSemanticType.SOLO -> SemanticReadingState(currentSemantic, "Solo", "Improvisação", PresentationPriority.HIGH)
            MusicalSemanticType.CODA -> SemanticReadingState(currentSemantic, "Coda", "Encerramento especial", PresentationPriority.HIGH)
            MusicalSemanticType.ENDING -> SemanticReadingState(currentSemantic, "Final", "Última execução", PresentationPriority.HIGH)
            MusicalSemanticType.OUTRO -> SemanticReadingState(currentSemantic, "Outro", "Conclusão", PresentationPriority.HIGH)
            MusicalSemanticType.VAMP -> SemanticReadingState(currentSemantic, "Vamp", "Vamp / Repetição", PresentationPriority.MEDIUM)
            MusicalSemanticType.UNKNOWN -> SemanticReadingState(currentSemantic, "Seção", "Navegação assistida", PresentationPriority.LOW)
        }
    }
}
