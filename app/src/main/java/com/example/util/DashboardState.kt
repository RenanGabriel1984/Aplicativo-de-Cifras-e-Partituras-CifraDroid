package com.example.util

data class DashboardState(
    val currentSong: String,
    val currentSection: String,
    val nextSection: String?,
    val currentPass: MusicalPass,
    val nextInstruction: String?,
    val pagesUntilInstruction: Int?,
    val repertoireProgress: Int,
    val repertoireTotal: Int,
    val elapsedTime: Long,
    val sessionActive: Boolean
)
