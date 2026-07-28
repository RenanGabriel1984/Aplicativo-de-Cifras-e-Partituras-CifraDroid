package com.example.util

object PerformanceIntelligenceEngine {
    fun evaluate(
        timeline: MusicalTimeline,
        musicalSemantics: List<MusicalSemanticBlock>,
        guidanceState: AdaptiveGuidance?,
        musicalIntent: MusicalIntent?,
        conductorState: ConductorState?,
        dashboardState: DashboardState,
        currentPage: Int
    ): PerformanceIntelligence {
        val currentPass = timeline.currentPass
        val isFinalPass = currentPass == MusicalPass.FINAL_PASS
        
        val currentSemantic = musicalSemantics.firstOrNull { currentPage in it.startPage..it.endPage }
        val semanticType = currentSemantic?.type ?: MusicalSemanticType.UNKNOWN
        val isEnding = semanticType == MusicalSemanticType.ENDING || semanticType == MusicalSemanticType.CODA

        var title = musicalIntent?.title ?: "Sessão Ativa"
        var subtitle = musicalIntent?.description ?: "Acompanhamento musical"
        var confidence = musicalIntent?.confidence ?: 50
        
        var attention = AttentionLevel.LOW
        var isCritical = false

        when (semanticType) {
            MusicalSemanticType.ENDING, MusicalSemanticType.CODA -> {
                attention = AttentionLevel.CRITICAL
                isCritical = true
            }
            MusicalSemanticType.SOLO -> attention = AttentionLevel.HIGH
            MusicalSemanticType.BRIDGE, MusicalSemanticType.CHORUS -> attention = AttentionLevel.MEDIUM
            else -> attention = AttentionLevel.LOW
        }

        if (guidanceState != null) {
            when (guidanceState.level) {
                GuidanceLevel.CRITICAL -> {
                    attention = AttentionLevel.CRITICAL
                    isCritical = true
                    title = guidanceState.title
                    subtitle = guidanceState.message
                }
                GuidanceLevel.IMPORTANT -> {
                    if (attention.ordinal < AttentionLevel.HIGH.ordinal) {
                        attention = AttentionLevel.HIGH
                        title = guidanceState.title
                        subtitle = guidanceState.message
                    }
                }
                GuidanceLevel.NORMAL -> {
                    if (attention.ordinal < AttentionLevel.MEDIUM.ordinal) {
                        attention = AttentionLevel.MEDIUM
                    }
                }
                GuidanceLevel.SUBTLE -> {}
            }
        }
        
        if (isFinalPass) {
            if (attention.ordinal < AttentionLevel.HIGH.ordinal) {
                attention = AttentionLevel.HIGH
            }
            if (isEnding) {
                attention = AttentionLevel.CRITICAL
                isCritical = true
            }
        }

        var nextEvent: String? = null
        var pagesAhead: Int? = null

        if (conductorState != null) {
            val activeIndex = conductorState.currentSection
            if (activeIndex in 0 until conductorState.sections.lastIndex) {
                val nextSection = conductorState.sections[activeIndex + 1]
                nextEvent = nextSection.type.name.lowercase().replaceFirstChar { it.uppercase() }
                pagesAhead = nextSection.startPage - currentPage
                if (pagesAhead < 0) pagesAhead = 0
            }
        }

        return PerformanceIntelligence(
            title = title,
            subtitle = subtitle,
            attention = attention,
            confidence = confidence,
            nextEvent = nextEvent,
            pagesAhead = pagesAhead,
            isFinalPass = isFinalPass,
            isEnding = isEnding,
            isCritical = isCritical
        )
    }
}
