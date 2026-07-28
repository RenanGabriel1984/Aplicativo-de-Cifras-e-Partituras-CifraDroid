package com.example.util

object MusicalCompanionEngine {
    fun evaluate(
        intelligence: PerformanceIntelligence?,
        guidance: AdaptiveGuidance?,
        intent: MusicalIntent?,
        timeline: MusicalTimeline,
        semanticReading: SemanticReadingState?,
        conductor: ConductorState?
    ): MusicalCompanionMessage {
        val currentSection = conductor?.sections?.getOrNull(conductor.currentSection)
        val type = currentSection?.type

        if (type == MusicalSemanticType.CODA) {
            return MusicalCompanionMessage("Coda", "Final aproximando", CompanionTone.ENDING)
        }
        if (type == MusicalSemanticType.ENDING) {
            return MusicalCompanionMessage("Encerramento", "Prepare-se para concluir", CompanionTone.ENDING)
        }
        if (intelligence?.isFinalPass == true) {
            return MusicalCompanionMessage("Última repetição", "Prepare-se", CompanionTone.WARNING)
        }
        if (type == MusicalSemanticType.SOLO) {
            return MusicalCompanionMessage("Solo", "Entrada instrumental", CompanionTone.CLIMAX)
        }
        if (type == MusicalSemanticType.CHORUS) {
            return MusicalCompanionMessage("Refrão", "Trecho principal", CompanionTone.CLIMAX)
        }
        if (type == MusicalSemanticType.BRIDGE) {
            return MusicalCompanionMessage("Ponte", "Mudança de seção", CompanionTone.WARNING)
        }

        if (guidance != null) {
            when (guidance.level) {
                GuidanceLevel.CRITICAL, GuidanceLevel.IMPORTANT -> return MusicalCompanionMessage("Cuidado", "Transição importante", CompanionTone.WARNING)
                GuidanceLevel.NORMAL -> return MusicalCompanionMessage("Prepare-se", "Próxima seção", CompanionTone.GUIDE)
                GuidanceLevel.SUBTLE -> {}
            }
        }

        return MusicalCompanionMessage("Continue", "Leitura estável", CompanionTone.CALM)
    }
}
