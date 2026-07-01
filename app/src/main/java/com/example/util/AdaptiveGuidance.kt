package com.example.util

enum class GuidanceLevel {
    SUBTLE,
    NORMAL,
    IMPORTANT,
    CRITICAL
}

data class AdaptiveGuidance(
    val title: String,
    val message: String,
    val level: GuidanceLevel
)
