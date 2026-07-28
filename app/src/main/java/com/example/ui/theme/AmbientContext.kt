package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class AmbientContext(
    val mode: AmbientMode,
    val glassTint: Color,
    val glassOpacity: Float,
    val dashboardGlow: Dp,
    val companionEmphasis: Float,
    val guidanceEmphasis: Float,
    val conductorEmphasis: Float,
    val backgroundIntensity: Float
)
