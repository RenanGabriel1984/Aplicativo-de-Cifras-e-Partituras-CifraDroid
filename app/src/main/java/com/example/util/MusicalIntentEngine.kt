package com.example.util

import kotlin.math.min

object MusicalIntentEngine {
    fun evaluate(
        currentPage: Int,
        musicalStructure: MusicalStructure,
        musicalSemantics: List<MusicalSemanticBlock>,
        musicalTimeline: MusicalTimeline,
        adaptiveGuidance: AdaptiveGuidance?,
        scoreRelationships: List<ScoreRelationship>
    ): MusicalIntent {
        val currentBlock = musicalSemantics.firstOrNull { currentPage in it.startPage..it.endPage }
        val semanticType = currentBlock?.type ?: MusicalSemanticType.UNKNOWN
        val currentPass = musicalTimeline.currentPass
        
        var type = MusicalIntentType.UNKNOWN
        var title = "Desconhecido"
        var description = "Intenção não detectada"
        var confidence = 40

        when (semanticType) {
            MusicalSemanticType.INTRO -> {
                type = MusicalIntentType.INTRODUCTION
                title = "Introdução"
                description = "Estabelecendo a base"
                confidence = 80
            }
            MusicalSemanticType.VERSE -> {
                if (currentPass == MusicalPass.FIRST_PASS) {
                    type = MusicalIntentType.BUILD_UP
                    title = "Construção"
                    description = "Desenvolvimento do verso"
                    confidence = 75
                } else {
                    type = MusicalIntentType.MAIN_THEME
                    title = "Tema Principal"
                    description = "Verso consolidado"
                    confidence = 85
                }
            }
            MusicalSemanticType.CHORUS -> {
                if (currentPass == MusicalPass.FIRST_PASS) {
                    type = MusicalIntentType.MAIN_THEME
                    title = "Tema Principal"
                    description = "Refrão inicial"
                    confidence = 90
                } else {
                    type = MusicalIntentType.CHORUS_PEAK
                    title = "Ápice"
                    description = "Clímax do refrão"
                    confidence = 95
                }
            }
            MusicalSemanticType.BRIDGE -> {
                type = MusicalIntentType.TRANSITION
                title = "Transição"
                description = "Ponte harmônica"
                confidence = 85
            }
            MusicalSemanticType.SOLO -> {
                type = MusicalIntentType.SOLO_SECTION
                title = "Solo"
                description = "Seção de improviso"
                confidence = 95
            }
            MusicalSemanticType.VAMP -> {
                type = MusicalIntentType.VAMP
                title = "Vamp"
                description = "Repetição cíclica"
                confidence = 85
            }
            MusicalSemanticType.ENDING -> {
                type = MusicalIntentType.ENDING
                title = "Final"
                description = "Encerramento"
                confidence = 100
            }
            MusicalSemanticType.CODA -> {
                type = MusicalIntentType.FINAL_CODA
                title = "Coda Final"
                description = "Trecho final exclusivo"
                confidence = 100
            }
            MusicalSemanticType.OUTRO -> {
                type = MusicalIntentType.RESOLUTION
                title = "Resolução"
                description = "Conclusão"
                confidence = 85
            }
            MusicalSemanticType.UNKNOWN -> {
                type = MusicalIntentType.UNKNOWN
                title = "Seção"
                description = "Navegação"
                confidence = 40
            }
        }

        // Heurísticas adicionais
        val hasFine = currentBlock?.markers?.any { it.type == ScoreMarkerType.FINE } == true
        if (hasFine) {
            confidence = 100
        }

        if (currentPass == MusicalPass.FINAL_PASS) {
            confidence = min(100, confidence + 10)
        }

        return MusicalIntent(
            type = type,
            title = title,
            description = description,
            confidence = confidence
        )
    }
}
