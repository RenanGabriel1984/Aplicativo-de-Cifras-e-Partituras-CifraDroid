package com.example.domain.analysis

data class MusicalAnalysis(
    val statistics: MusicalAnalysisStatistics,
    val sequence: List<String>,
    val estimatedDurationSeconds: Int,
    val complexity: String,
    val alerts: List<String>
)
