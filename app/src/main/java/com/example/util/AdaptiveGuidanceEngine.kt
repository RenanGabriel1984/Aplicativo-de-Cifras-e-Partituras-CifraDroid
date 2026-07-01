package com.example.util

object AdaptiveGuidanceEngine {
    fun produceGuidance(
        currentPage: Int,
        structure: MusicalStructure,
        semantics: List<MusicalSemanticBlock>,
        timeline: MusicalTimeline,
        relationships: List<ScoreRelationship>,
        markers: List<ScoreMarker>
    ): AdaptiveGuidance? {
        val lookaheadPages = (currentPage + 1)..(currentPage + 3)
        
        val upcomingSemantics = semantics.filter { it.startPage in lookaheadPages }
        
        for (semantic in upcomingSemantics) {
            when (semantic.type) {
                MusicalSemanticType.SOLO -> return AdaptiveGuidance("Prepare-se", "Solo em breve", GuidanceLevel.IMPORTANT)
                MusicalSemanticType.CODA -> return AdaptiveGuidance("Prepare-se", "Coda em aproximação", GuidanceLevel.IMPORTANT)
                MusicalSemanticType.ENDING -> return AdaptiveGuidance("Final", "Encerramento próximo", GuidanceLevel.CRITICAL)
                MusicalSemanticType.CHORUS -> return AdaptiveGuidance("Refrão", "Trecho principal", GuidanceLevel.NORMAL)
                MusicalSemanticType.BRIDGE -> return AdaptiveGuidance("Ponte", "Transição musical", GuidanceLevel.NORMAL)
                else -> {}
            }
        }
        
        if (timeline.currentPass == MusicalPass.SECOND_PASS) {
            return AdaptiveGuidance("Última repetição", "Prepare-se", GuidanceLevel.IMPORTANT)
        } else if (timeline.currentPass == MusicalPass.THIRD_PASS || timeline.currentPass == MusicalPass.FINAL_PASS) {
            return AdaptiveGuidance("Passagem final", "Encerramento", GuidanceLevel.CRITICAL)
        }

        return null
    }
}
