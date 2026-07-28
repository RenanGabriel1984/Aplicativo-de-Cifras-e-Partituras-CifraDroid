package com.example.util

data class MusicalIntent(
    val type: MusicalIntentType,
    val title: String,
    val description: String,
    val confidence: Int
)
