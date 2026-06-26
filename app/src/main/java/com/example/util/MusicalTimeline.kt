package com.example.util

data class ExecutedRelationship(
    val relationshipId: String,
    val originPage: Int,
    val destinationPage: Int,
    val executionTime: Long,
    val executionOrder: Int,
    val pass: MusicalPass
)

data class MusicalTimeline(
    val currentPage: Int = 0,
    val visitedPages: Set<Int> = emptySet(),
    val executedRelationships: List<ExecutedRelationship> = emptyList(),
    val currentPass: MusicalPass = MusicalPass.FIRST_PASS,
    val visitedMarkers: Set<String> = emptySet(),
    val timelineFinished: Boolean = false
)
