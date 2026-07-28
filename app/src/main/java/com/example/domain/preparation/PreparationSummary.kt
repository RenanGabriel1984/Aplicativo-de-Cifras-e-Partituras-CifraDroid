package com.example.domain.preparation

data class PreparationSummary(
    val ready: Boolean,
    val missingFields: Int,
    val estimatedPerformanceReady: Boolean
)
