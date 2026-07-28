package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ui.layout.ResponsiveStageState
import com.example.util.AdaptiveGuidance
import com.example.util.FlowContext
import com.example.util.FlowState
import com.example.util.MusicalCompanionMessage
import com.example.util.MusicalIntent
import com.example.util.MusicalTimeline

@Composable
fun rememberVisualCohesionEngine(
    flowContext: FlowContext,
    responsiveState: ResponsiveStageState,
    guidance: AdaptiveGuidance?,
    companion: MusicalCompanionMessage?,
    intent: MusicalIntent?,
    timeline: MusicalTimeline?,
    minimalState: PerformanceMinimalState? = null
): VisualContext {
    return remember(flowContext.state, responsiveState.layout, guidance, companion, intent, timeline, minimalState) {
        val companionPriority = when {
            companion != null && flowContext.state == FlowState.IDLE -> VisualPriority.PRIMARY
            companion != null -> VisualPriority.SECONDARY
            else -> VisualPriority.BACKGROUND
        }
        
        val guidancePriority = when {
            guidance != null && flowContext.state == FlowState.FOCUS -> VisualPriority.PRIMARY
            guidance != null -> VisualPriority.SECONDARY
            else -> VisualPriority.BACKGROUND
        }
        
        val dashboardPriority = when (flowContext.state) {
            FlowState.IDLE -> VisualPriority.PRIMARY
            FlowState.TRANSITION -> VisualPriority.SECONDARY
            FlowState.FOCUS, FlowState.READING, FlowState.PERFORMANCE -> VisualPriority.TERTIARY
            FlowState.IMMERSION -> VisualPriority.BACKGROUND
        }
        
        val conductorPriority = when (flowContext.state) {
            FlowState.IMMERSION -> VisualPriority.PRIMARY
            FlowState.FOCUS, FlowState.READING, FlowState.PERFORMANCE -> VisualPriority.SECONDARY
            else -> VisualPriority.TERTIARY
        }
        
        val sessionPriority = when (flowContext.state) {
            FlowState.TRANSITION -> VisualPriority.PRIMARY
            else -> VisualPriority.SECONDARY
        }
        
        fun applyMinimalState(priority: VisualPriority, visibility: MinimalVisibility?): VisualPriority {
            return when (visibility) {
                MinimalVisibility.HIDDEN -> VisualPriority.HIDDEN
                MinimalVisibility.REDUCED -> if (priority == VisualPriority.PRIMARY || priority == VisualPriority.SECONDARY) VisualPriority.TERTIARY else priority
                else -> priority
            }
        }
        
        VisualContext(
            companionPriority = applyMinimalState(companionPriority, minimalState?.companion),
            guidancePriority = applyMinimalState(guidancePriority, minimalState?.guidance),
            dashboardPriority = applyMinimalState(dashboardPriority, minimalState?.dashboard),
            conductorPriority = applyMinimalState(conductorPriority, minimalState?.conductor),
            sessionPriority = applyMinimalState(sessionPriority, minimalState?.session),
            opacityMap = VisualHierarchy.opacity,
            elevationMap = VisualHierarchy.elevation,
            scaleMap = VisualHierarchy.scale
        )
    }
}

fun Modifier.visualCohesion(priority: VisualPriority, context: VisualContext): Modifier = composed {
    val opacity by androidx.compose.animation.core.animateFloatAsState(
        targetValue = context.opacityMap[priority] ?: 1f, 
        animationSpec = com.example.ui.theme.AppMotion.Smooth,
        label = "opacity"
    )
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = context.scaleMap[priority] ?: 1f, 
        animationSpec = com.example.ui.theme.AppMotion.Smooth,
        label = "scale"
    )
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = context.elevationMap[priority] ?: 0.dp, 
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "elevation"
    )

    this.then(Modifier.graphicsLayer {
        this.alpha = opacity
        this.scaleX = scale
        this.scaleY = scale
        this.shadowElevation = elevation.toPx()
    })
}
