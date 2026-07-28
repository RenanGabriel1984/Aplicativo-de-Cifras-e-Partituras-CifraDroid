package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppColors
import com.example.ui.theme.AppElevation
import com.example.ui.theme.AppShapes

@Composable
fun ConductorSegment(
    type: com.example.util.MusicalSemanticType,
    active: Boolean,
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    val targetBackgroundColor = when (type) {
        com.example.util.MusicalSemanticType.INTRO -> AppColors.Primary
        com.example.util.MusicalSemanticType.VERSE -> MaterialTheme.colorScheme.surfaceVariant
        com.example.util.MusicalSemanticType.CHORUS -> AppColors.Amber
        com.example.util.MusicalSemanticType.BRIDGE -> AppColors.Tertiary
        com.example.util.MusicalSemanticType.SOLO -> AppColors.Secondary
        com.example.util.MusicalSemanticType.CODA -> AppColors.Error
        com.example.util.MusicalSemanticType.OUTRO -> MaterialTheme.colorScheme.errorContainer
        com.example.util.MusicalSemanticType.ENDING -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val animatedColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = spring(),
        label = "segment_color"
    )

    val targetAlpha = if (completed && !active) 0.35f else 1f
    val animatedAlpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = spring(),
        label = "segment_alpha"
    )

    val targetScale = if (active) 1.05f else 1f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(),
        label = "segment_scale"
    )

    val elevation = if (active) AppElevation.Level3 else AppElevation.Level0

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .scale(animatedScale)
            .alpha(animatedAlpha),
        shape = AppShapes.Small,
        color = animatedColor,
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier.fillMaxHeight()
        ) {
            if (active) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(2.dp, MaterialTheme.colorScheme.onSurface, AppShapes.Small)
                )
            }
        }
    }
}
