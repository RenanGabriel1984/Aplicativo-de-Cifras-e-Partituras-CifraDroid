package com.example.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberThemeTransitionEngine(targetTheme: StageThemeType): ThemeTransitionState {
    var previousTheme by remember { mutableStateOf<StageThemeType?>(null) }
    var currentTheme by remember { mutableStateOf(targetTheme) }
    var isTransitioning by remember { mutableStateOf(false) }
    var transitionTarget by remember { mutableStateOf(targetTheme) }

    if (transitionTarget != targetTheme) {
        previousTheme = transitionTarget
        transitionTarget = targetTheme
        isTransitioning = true
    }

    val progress by animateFloatAsState(
        targetValue = if (isTransitioning) 1f else 0f,
        animationSpec = if (targetTheme == StageThemeType.OLED) snap() else tween(600),
        finishedListener = {
            if (isTransitioning && it == 1f) {
                currentTheme = transitionTarget
                isTransitioning = false
                previousTheme = null
            }
        },
        label = "theme_progress"
    )

    return ThemeTransitionState(
        isTransitioning = isTransitioning,
        previousTheme = previousTheme,
        currentTheme = transitionTarget,
        progress = progress
    )
}
