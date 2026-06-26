package com.example.util

object MusicalTimelineEngine {
    fun updatePage(
        timeline: MusicalTimeline,
        page: Int,
        markersInPage: List<ScoreMarker>
    ): MusicalTimeline {
        val newVisitedPages = timeline.visitedPages + page
        val newVisitedMarkers = timeline.visitedMarkers + markersInPage.map { it.id }
        
        var isFinished = timeline.timelineFinished
        if (markersInPage.any { it.type == ScoreMarkerType.FINE } && timeline.currentPass != MusicalPass.FIRST_PASS) {
            isFinished = true
        }

        return timeline.copy(
            currentPage = page,
            visitedPages = newVisitedPages,
            visitedMarkers = newVisitedMarkers,
            timelineFinished = isFinished
        )
    }

    fun executeRelationship(
        timeline: MusicalTimeline,
        relationship: ScoreRelationship
    ): MusicalTimeline {
        val newExecuted = timeline.executedRelationships.toMutableList()
        newExecuted.add(
            ExecutedRelationship(
                relationshipId = relationship.sourceMarkerId,
                originPage = relationship.sourcePage,
                destinationPage = relationship.targetPage ?: 0,
                executionTime = System.currentTimeMillis(),
                executionOrder = newExecuted.size + 1,
                pass = timeline.currentPass
            )
        )

        var newPass = timeline.currentPass
        var isFinished = timeline.timelineFinished

        when (relationship.relationshipType) {
            RelationshipType.DA_CAPO_TO_START -> {
                newPass = newPass.next()
            }
            RelationshipType.DAL_SEGNO_TO_SEGNO -> {
                newPass = newPass.next()
            }
            RelationshipType.AL_FINE_TO_FINE -> {
                isFinished = true
            }
            else -> {}
        }

        return timeline.copy(
            executedRelationships = newExecuted,
            currentPass = newPass,
            timelineFinished = isFinished
        )
    }
}
