package com.example.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.unit.Dp

data class ImmersiveContext(
    val mode: ImmersiveMode,
    val backgroundAlpha: Float,
    val glassOpacity: Float,
    val blurRadius: Dp,
    val dashboardScale: Float,
    val dashboardElevation: Dp,
    val guidanceVisibility: Float,
    val companionVisibility: Float,
    val conductorVisibility: Float,
    val animationProfile: AnimationSpec<Float>
)
