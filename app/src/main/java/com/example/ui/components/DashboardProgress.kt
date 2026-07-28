package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppShapes
import com.example.ui.theme.LocalStageTheme

@Composable
fun DashboardProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val theme = LocalStageTheme.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = theme.motionProfile,
        label = "progress_animation"
    )

    val trackColor by animateColorAsState(targetValue = theme.badgeColor, label = "track")
    val indicatorColor by animateColorAsState(targetValue = theme.progressColor, label = "indicator")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(AppShapes.Pill)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(indicatorColor)
        )
    }
}
