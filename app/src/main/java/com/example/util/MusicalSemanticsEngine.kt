package com.example.util

import java.util.UUID

object MusicalSemanticsEngine {
    fun inferSemantics(
        structure: MusicalStructure,
        markers: List<ScoreMarker>,
        timeline: MusicalTimeline
    ): List<MusicalSemanticBlock> {
        return structure.sections.mapIndexed { index, section ->
            val sectionMarkers = markers.filter { it.page in section.startPage..section.endPage }
            
            var inferredType = MusicalSemanticType.UNKNOWN
            var confidence = 0.0f
            
            // Heuristics
            if (sectionMarkers.any { it.type == ScoreMarkerType.SEGNO }) {
                inferredType = MusicalSemanticType.CHORUS
                confidence = 0.8f
            } else if (sectionMarkers.any { it.type == ScoreMarkerType.CODA }) {
                inferredType = MusicalSemanticType.CODA
                confidence = 0.9f
            } else if (sectionMarkers.any { it.type == ScoreMarkerType.FINE }) {
                inferredType = MusicalSemanticType.ENDING
                confidence = 1.0f
            } else if (sectionMarkers.any { it.type == ScoreMarkerType.TO_CODA }) {
                inferredType = MusicalSemanticType.BRIDGE
                confidence = 0.7f
            } else if (sectionMarkers.any { it.type == ScoreMarkerType.DA_CAPO }) {
                inferredType = MusicalSemanticType.VERSE
                confidence = 0.6f
            } else {
                // Determine by position
                if (index == 0) {
                    inferredType = MusicalSemanticType.INTRO
                    confidence = 0.5f
                } else if (index == structure.sections.size - 1) {
                    inferredType = MusicalSemanticType.OUTRO
                    confidence = 0.4f
                } else {
                    inferredType = MusicalSemanticType.VERSE
                    confidence = 0.3f
                }
            }
            
            MusicalSemanticBlock(
                id = UUID.randomUUID().toString(),
                type = inferredType,
                startPage = section.startPage,
                endPage = section.endPage,
                confidence = confidence,
                markers = sectionMarkers
            )
        }
    }
}
