package com.example.domain.performance

import com.example.domain.preparation.PreparationDestination

data class PerformancePreparation(
    val id: String,
    val title: String,
    val category: PreparationDestination,
    val date: Long,
    val songs: List<PerformanceSong>,
    val statistics: PerformanceStatistics,
    val readiness: PerformanceReadiness
)
