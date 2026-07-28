package com.example.util

enum class CompanionTone {
    CALM,
    GUIDE,
    WARNING,
    CLIMAX,
    ENDING
}

data class MusicalCompanionMessage(
    val title: String,
    val message: String,
    val tone: CompanionTone
)
