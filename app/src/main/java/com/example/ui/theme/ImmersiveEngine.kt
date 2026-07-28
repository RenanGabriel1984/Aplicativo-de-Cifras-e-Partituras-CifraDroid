package com.example.ui.theme

import androidx.compose.ui.unit.dp
import com.example.util.*

fun buildImmersiveContext(
    flowContext: FlowContext,
    performanceIntelligence: PerformanceIntelligence?,
    musicalIntent: MusicalIntent?,
    adaptiveGuidance: AdaptiveGuidance?,
    companionState: MusicalCompanionMessage?,
    timeline: MusicalTimeline?,
    conductorState: ConductorState?,
    minimalState: PerformanceMinimalState? = null
): ImmersiveContext {
    val isClimax = musicalIntent?.type == MusicalIntentType.CHORUS_PEAK ||
                   companionState?.tone == CompanionTone.CLIMAX ||
                   performanceIntelligence?.attention == AttentionLevel.HIGH
    
    val isEnding = musicalIntent?.type == MusicalIntentType.ENDING ||
                   musicalIntent?.type == MusicalIntentType.FINAL_CODA ||
                   performanceIntelligence?.isEnding == true ||
                   timeline?.currentPass == MusicalPass.FINAL_PASS ||
                   companionState?.tone == CompanionTone.ENDING
                   
    val mode = when {
        isClimax -> ImmersiveMode.CLIMAX
        isEnding -> ImmersiveMode.ENDING
        flowContext.state == FlowState.FOCUS -> ImmersiveMode.FOCUS
        flowContext.state == FlowState.PERFORMANCE -> ImmersiveMode.PERFORMANCE
        flowContext.state == FlowState.READING -> ImmersiveMode.READING
        else -> ImmersiveMode.FLOW // IDLE, TRANSITION, IMMERSION
    }
    
    var backgroundAlpha = 0.5f
    var glassOpacity = 0.75f
    var blurRadius = 16.dp
    var dashboardScale = 1.0f
    var dashboardElevation = 4.dp
    var guidanceVisibility = 0f
    var companionVisibility = 0f
    var conductorVisibility = 0f
    var animationProfile: androidx.compose.animation.core.AnimationSpec<Float> = AppMotion.Smooth
    
    when (mode) {
        ImmersiveMode.CLIMAX -> {
            dashboardScale = 1.03f
            glassOpacity = 0.95f
            guidanceVisibility = 1.0f
            companionVisibility = 1.0f
            conductorVisibility = 1.0f
            backgroundAlpha = 0.8f
            blurRadius = 24.dp
            dashboardElevation = 8.dp
            animationProfile = AppMotion.LowBounce
        }
        ImmersiveMode.ENDING -> {
            glassOpacity = 0.98f
            dashboardScale = 0.98f
            guidanceVisibility = 1.0f
            companionVisibility = 1.0f
            conductorVisibility = 0.8f
            backgroundAlpha = 0.9f
            blurRadius = 32.dp
            dashboardElevation = 2.dp
            animationProfile = AppMotion.Performance
        }
        ImmersiveMode.FLOW -> {
            glassOpacity = 0.6f
            dashboardScale = 1.0f
            guidanceVisibility = 0.8f
            companionVisibility = 0.4f
            conductorVisibility = 1.0f
            backgroundAlpha = 0.4f
            blurRadius = 16.dp
            dashboardElevation = 4.dp
            animationProfile = AppMotion.Smooth
        }
        ImmersiveMode.READING -> {
            glassOpacity = 0.4f
            dashboardScale = 0.95f
            guidanceVisibility = 0.5f
            companionVisibility = 0.3f
            conductorVisibility = 0.5f
            backgroundAlpha = 0.2f
            blurRadius = 8.dp
            dashboardElevation = 2.dp
            animationProfile = AppMotion.Smooth
        }
        ImmersiveMode.PERFORMANCE -> {
            glassOpacity = 0.8f
            dashboardScale = 1.0f
            guidanceVisibility = 1.0f
            companionVisibility = 1.0f
            conductorVisibility = 1.0f
            backgroundAlpha = 0.6f
            blurRadius = 16.dp
            dashboardElevation = 6.dp
            animationProfile = AppMotion.Performance
        }
        ImmersiveMode.FOCUS -> {
            glassOpacity = 0.3f
            dashboardScale = 0.95f
            guidanceVisibility = 0.2f
            companionVisibility = 0.2f
            conductorVisibility = 0.5f
            backgroundAlpha = 0.1f
            blurRadius = 4.dp
            dashboardElevation = 0.dp
            animationProfile = AppMotion.Smooth
        }
    }
    
    if (adaptiveGuidance == null) guidanceVisibility = 0f
    if (companionState == null) companionVisibility = 0f
    if (conductorState == null) conductorVisibility = 0f
    
    val guidanceMultiplier = when (minimalState?.guidance) {
        MinimalVisibility.HIDDEN -> 0f
        MinimalVisibility.REDUCED -> 0.3f
        else -> 1f
    }
    val companionMultiplier = when (minimalState?.companion) {
        MinimalVisibility.HIDDEN -> 0f
        MinimalVisibility.REDUCED -> 0.3f
        else -> 1f
    }
    val conductorMultiplier = when (minimalState?.conductor) {
        MinimalVisibility.HIDDEN -> 0f
        MinimalVisibility.REDUCED -> 0.3f
        else -> 1f
    }
    
    guidanceVisibility *= guidanceMultiplier
    companionVisibility *= companionMultiplier
    conductorVisibility *= conductorMultiplier

    return ImmersiveContext(
        mode = mode,
        backgroundAlpha = backgroundAlpha,
        glassOpacity = glassOpacity,
        blurRadius = blurRadius,
        dashboardScale = dashboardScale,
        dashboardElevation = dashboardElevation,
        guidanceVisibility = guidanceVisibility,
        companionVisibility = companionVisibility,
        conductorVisibility = conductorVisibility,
        animationProfile = animationProfile
    )
}
