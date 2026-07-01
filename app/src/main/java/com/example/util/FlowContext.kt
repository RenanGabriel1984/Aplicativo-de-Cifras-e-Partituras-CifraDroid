package com.example.util

data class FlowContext(
    val state: FlowState,
    val showHud: Boolean,
    val opacity: Float,
    val allowGestures: Boolean,
    val showMarkers: Boolean,
    val reduceAnimations: Boolean,
    val cueIntensity: CueLevel
)
