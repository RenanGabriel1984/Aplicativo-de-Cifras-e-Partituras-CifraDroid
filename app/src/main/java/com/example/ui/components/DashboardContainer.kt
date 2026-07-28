package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppGlass
import com.example.ui.theme.AppShapes
import com.example.ui.theme.AppMotion
import com.example.ui.theme.AppSpacing
import com.example.ui.theme.LocalStageTheme

@Composable
fun DashboardContainer(
    modifier: Modifier = Modifier,
    immersiveContext: com.example.ui.theme.ImmersiveContext? = null,
    ambientContext: com.example.ui.theme.AmbientContext? = null,
    minimalState: com.example.ui.theme.PerformanceMinimalState? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalStageTheme.current
    val stageState = com.example.ui.layout.rememberResponsiveStageEngine()
    
    val minimalAlphaMultiplier = when (minimalState?.dashboard) {
        com.example.ui.theme.MinimalVisibility.HIDDEN -> 0f
        com.example.ui.theme.MinimalVisibility.REDUCED -> 0.3f
        else -> 1f
    }
    
    val targetOpacity = (ambientContext?.glassOpacity ?: immersiveContext?.glassOpacity ?: theme.glassOpacity) * minimalAlphaMultiplier
    val glassOpacity by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetOpacity,
        animationSpec = immersiveContext?.animationProfile ?: AppMotion.Smooth,
        label = "glassOpacity"
    )
    
    val blurRadius = ambientContext?.dashboardGlow ?: immersiveContext?.blurRadius ?: 16.dp
    
    val targetTint = ambientContext?.glassTint ?: theme.backgroundColor
    val glassTint by androidx.compose.animation.animateColorAsState(
        targetValue = targetTint,
        animationSpec = androidx.compose.animation.core.tween(600),
        label = "glassTint"
    )
    
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = immersiveContext?.dashboardScale ?: 1f,
        animationSpec = immersiveContext?.animationProfile ?: AppMotion.Smooth,
        label = "dashboardScale"
    )
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = ambientContext?.dashboardGlow ?: immersiveContext?.dashboardElevation ?: 0.dp,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium),
        label = "dashboardElevation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.shadowElevation = elevation.toPx()
            }
            .widthIn(max = stageState.dashboardWidth)
            .then(
                AppGlass.glassPerformance(
                    color = glassTint,
                    shape = AppShapes.Large,
                    alpha = glassOpacity
                )
            )
            .padding(AppSpacing.LG)
    ) {
        content()
    }
}
