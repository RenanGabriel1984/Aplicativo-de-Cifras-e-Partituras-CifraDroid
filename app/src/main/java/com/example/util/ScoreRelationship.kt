package com.example.util

enum class RelationshipType {
    DA_CAPO_TO_START,
    DAL_SEGNO_TO_SEGNO,
    TO_CODA_TO_CODA,
    AL_FINE_TO_FINE,
    UNRESOLVED
}

data class ScoreRelationship(
    val sourceMarkerId: String,
    val targetMarkerId: String?,
    val sourcePage: Int,
    val targetPage: Int?,
    val relationshipType: RelationshipType
)
