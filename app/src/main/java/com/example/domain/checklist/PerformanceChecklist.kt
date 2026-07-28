package com.example.domain.checklist

data class PerformanceChecklist(
    val performanceId: String,
    val items: List<ChecklistItem>,
    val completionPercentage: Float,
    val pendingItems: Int,
    val warningItems: Int
)
