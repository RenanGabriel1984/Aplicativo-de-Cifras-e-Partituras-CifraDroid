package com.example.util

data class SemanticReadingState(
    val currentSemantic: MusicalSemanticType,
    val title: String,
    val subtitle: String,
    val priority: PresentationPriority
)
