package com.example.util

enum class MusicalExecutionState {
    NORMAL,
    WAITING_CONFIRMATION,
    EXECUTING,
    FINISHED
}

data class MusicalExecutionAction(
    val relationship: ScoreRelationship,
    val currentPage: Int,
    val targetPage: Int,
    val message: String
)

object MusicalExecutionEngine {
    fun evaluateCurrentPage(
        currentPage: Int,
        relationships: List<ScoreRelationship>,
        timeline: MusicalTimeline
    ): MusicalExecutionAction? {
        val relationship = relationships.firstOrNull { it.sourcePage == currentPage } ?: return null

        val alreadyExecutedInThisPass = timeline.executedRelationships.any {
            it.relationshipId == relationship.sourceMarkerId && it.pass == timeline.currentPass
        }
        if (alreadyExecutedInThisPass) return null

        val targetPage = relationship.targetPage ?: return null
        
        val message = when (relationship.relationshipType) {
            RelationshipType.DA_CAPO_TO_START -> "D.C.\n\nDeseja voltar ao início da música?"
            RelationshipType.DAL_SEGNO_TO_SEGNO -> "D.S.\n\nDeseja voltar ao Segno?"
            RelationshipType.TO_CODA_TO_CODA -> "To Coda\n\nDeseja ir para a Coda?"
            RelationshipType.AL_FINE_TO_FINE -> "Fine\n\nFim da música."
            RelationshipType.UNRESOLVED -> return null
        }

        return MusicalExecutionAction(
            relationship = relationship,
            currentPage = currentPage,
            targetPage = targetPage,
            message = message
        )
    }
}
