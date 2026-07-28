package com.example.ui.theme

enum class MinimalVisibility {
    VISIBLE,
    REDUCED,
    HIDDEN
}

data class PerformanceMinimalState(
    val dashboard: MinimalVisibility,
    val companion: MinimalVisibility,
    val guidance: MinimalVisibility,
    val conductor: MinimalVisibility,
    val hud: MinimalVisibility,
    val session: MinimalVisibility,
    val timeline: MinimalVisibility
)
