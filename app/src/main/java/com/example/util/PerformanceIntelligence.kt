package com.example.util

enum class AttentionLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class PerformanceIntelligence(
    val title: String,
    val subtitle: String,
    val attention: AttentionLevel,
    val confidence: Int,
    val nextEvent: String?,
    val pagesAhead: Int?,
    val isFinalPass: Boolean,
    val isEnding: Boolean,
    val isCritical: Boolean
)
