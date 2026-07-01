package com.example.util

enum class PresentationPriority {
    LOW, MEDIUM, HIGH
}

data class DashboardPresentation(
    val title: String,
    val subtitle: String,
    val badge: String?,
    val cue: String?,
    val emphasis: PresentationPriority
)
