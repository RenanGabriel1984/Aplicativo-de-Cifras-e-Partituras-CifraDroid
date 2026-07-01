package com.example.util

data class ReadingContext(
    val message: String?,
    val confidence: Float,
    val urgency: ContextUrgency
)

enum class ContextUrgency {
    LOW,
    MEDIUM,
    HIGH
}

object ReadingContextEngine {
    fun inferContext(
        structure: MusicalStructure?,
        timeline: MusicalTimeline?,
        currentPage: Int,
        markers: List<ScoreMarker>
    ): ReadingContext {
        val upcomingMarker = markers.firstOrNull { marker ->
            marker.page in (currentPage + 1)..(currentPage + 2) && 
            timeline?.visitedMarkers?.contains(marker.id) != true
        }

        if (upcomingMarker != null) {
            if (upcomingMarker.type == ScoreMarkerType.DAL_SEGNO || upcomingMarker.type == ScoreMarkerType.DA_CAPO || upcomingMarker.type == ScoreMarkerType.SEGNO) {
                return ReadingContext("Prepare-se para retorno", 1.0f, ContextUrgency.HIGH)
            }
            if (upcomingMarker.type == ScoreMarkerType.CODA || upcomingMarker.type == ScoreMarkerType.TO_CODA) {
                return ReadingContext("Entrando na Coda", 1.0f, ContextUrgency.HIGH)
            }
            if (upcomingMarker.type == ScoreMarkerType.FINE || upcomingMarker.type == ScoreMarkerType.AL_FINE) {
                return ReadingContext("Finalização próxima", 1.0f, ContextUrgency.HIGH)
            }
        }

        if (timeline != null && timeline.timelineFinished) {
            return ReadingContext("Encerramento", 1.0f, ContextUrgency.HIGH)
        }

        if (timeline != null && timeline.currentPass == MusicalPass.FINAL_PASS) {
            return ReadingContext("Passagem final", 1.0f, ContextUrgency.HIGH)
        }

        val currentSection = structure?.sections?.firstOrNull { currentPage in it.startPage..it.endPage }

        if (currentSection != null && currentSection.type == StructureType.ENDING) {
            return ReadingContext("Trecho final", 1.0f, ContextUrgency.MEDIUM)
        }

        if (timeline != null && timeline.currentPass == MusicalPass.SECOND_PASS) {
            return ReadingContext("Segunda passagem", 1.0f, ContextUrgency.MEDIUM)
        }

        if (currentSection != null && currentSection.type == StructureType.CHORUS) {
            return ReadingContext("Refrão", 1.0f, ContextUrgency.LOW)
        }

        if (currentPage == 0 && structure?.sections?.any { it.type == StructureType.CHORUS } == true) {
            return ReadingContext("Verso inicial", 1.0f, ContextUrgency.LOW)
        }

        return ReadingContext(null, 0f, ContextUrgency.LOW)
    }
}
