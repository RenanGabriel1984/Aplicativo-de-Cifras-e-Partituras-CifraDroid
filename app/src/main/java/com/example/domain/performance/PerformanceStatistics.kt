package com.example.domain.performance

data class PerformanceStatistics(
    val totalSongs: Int,
    val readySongs: Int,
    val pendingSongs: Int,
    val completionPercentage: Float
)
