package com.example.ui.theme

import androidx.compose.ui.unit.Dp

data class VisualContext(
    val companionPriority: VisualPriority,
    val guidancePriority: VisualPriority,
    val dashboardPriority: VisualPriority,
    val conductorPriority: VisualPriority,
    val sessionPriority: VisualPriority,
    val opacityMap: Map<VisualPriority, Float>,
    val elevationMap: Map<VisualPriority, Dp>,
    val scaleMap: Map<VisualPriority, Float>
)
