package com.example.util

enum class ScoreMarkerType {
    SEGNO,
    CODA,
    TO_CODA,
    DA_CAPO,
    DAL_SEGNO,
    FINE,
    AL_FINE,
    REPEAT_START,
    REPEAT_END,
    FIRST_ENDING,
    SECOND_ENDING,
    UNKNOWN
}

data class ScoreMarker(
    val id: String,
    val type: ScoreMarkerType,
    val page: Int,
    val text: String
)
