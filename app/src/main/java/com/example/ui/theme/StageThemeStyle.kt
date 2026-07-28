package com.example.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

data class StageThemeStyle(
    val backgroundColor: Color,
    val surfaceColor: Color,
    val glassOpacity: Float,
    val blur: Dp,
    val accentColor: Color,
    val badgeColor: Color,
    val progressColor: Color,
    val cueColor: Color,
    val shadowLevel: Dp,
    val motionProfile: AnimationSpec<Float>
)
