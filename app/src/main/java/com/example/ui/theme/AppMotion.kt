package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object AppMotion {
    val Fast = tween<Float>(durationMillis = 150, easing = LinearOutSlowInEasing)
    val Normal = tween<Float>(durationMillis = 300, easing = FastOutSlowInEasing)
    val Slow = tween<Float>(durationMillis = 450, easing = FastOutSlowInEasing)
    val Performance = tween<Float>(durationMillis = 700, easing = FastOutSlowInEasing)
    
    val LowBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    val Smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
