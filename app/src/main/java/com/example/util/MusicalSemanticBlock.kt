package com.example.util

data class MusicalSemanticBlock(
    val id: String,
    val type: MusicalSemanticType,
    val startPage: Int,
    val endPage: Int,
    val confidence: Float,
    val markers: List<ScoreMarker>
)
