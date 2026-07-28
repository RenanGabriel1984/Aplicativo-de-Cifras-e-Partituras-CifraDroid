package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.util.*

fun buildAmbientContext(
    flowContext: FlowContext,
    performanceIntelligence: PerformanceIntelligence?,
    musicalIntent: MusicalIntent?,
    adaptiveGuidance: AdaptiveGuidance?,
    companionState: MusicalCompanionMessage?,
    timeline: MusicalTimeline?,
    conductorState: ConductorState?,
    baseTheme: StageThemeStyle
): AmbientContext {
    val isChorus = musicalIntent?.type == MusicalIntentType.CHORUS_PEAK
    val isClimax = isChorus ||
                   companionState?.tone == CompanionTone.CLIMAX ||
                   performanceIntelligence?.attention == AttentionLevel.HIGH
    
    val isEnding = musicalIntent?.type == MusicalIntentType.ENDING ||
                   musicalIntent?.type == MusicalIntentType.FINAL_CODA ||
                   performanceIntelligence?.isEnding == true ||
                   timeline?.currentPass == MusicalPass.FINAL_PASS ||
                   companionState?.tone == CompanionTone.ENDING
                   
    val mode = when {
        isClimax -> AmbientMode.CLIMAX
        isEnding -> AmbientMode.ENDING
        flowContext.state == FlowState.FOCUS -> AmbientMode.FOCUS
        flowContext.state == FlowState.PERFORMANCE -> AmbientMode.PERFORMANCE
        flowContext.state == FlowState.READING || flowContext.state == FlowState.IDLE -> AmbientMode.CALM
        else -> AmbientMode.FLOW
    }

    var glassTint = baseTheme.surfaceColor
    var glassOpacity = baseTheme.glassOpacity
    var dashboardGlow = 0.dp
    var companionEmphasis = 0.5f
    var guidanceEmphasis = 0.5f
    var conductorEmphasis = 0.5f
    var backgroundIntensity = 0.5f
    
    when (mode) {
        AmbientMode.CLIMAX -> {
            glassTint = if (isChorus) Color(0xFFFFB300) else baseTheme.accentColor
            glassOpacity = 0.95f
            dashboardGlow = 16.dp
            companionEmphasis = 1.0f
            guidanceEmphasis = 1.0f
            conductorEmphasis = 1.0f
            backgroundIntensity = 0.9f
        }
        AmbientMode.ENDING -> {
            glassTint = baseTheme.surfaceColor
            glassOpacity = 0.9f
            dashboardGlow = 4.dp
            companionEmphasis = 1.0f
            guidanceEmphasis = 0.8f
            conductorEmphasis = 0.3f
            backgroundIntensity = 0.3f
        }
        AmbientMode.FOCUS -> {
            glassTint = baseTheme.backgroundColor
            glassOpacity = 0.2f
            dashboardGlow = 0.dp
            companionEmphasis = 0.2f
            guidanceEmphasis = 0.2f
            conductorEmphasis = 0.4f
            backgroundIntensity = 0.1f
        }
        AmbientMode.PERFORMANCE -> {
            glassTint = baseTheme.surfaceColor
            glassOpacity = 0.85f
            dashboardGlow = 8.dp
            companionEmphasis = 0.9f
            guidanceEmphasis = 0.9f
            conductorEmphasis = 0.9f
            backgroundIntensity = 0.7f
        }
        AmbientMode.FLOW -> {
            glassTint = baseTheme.surfaceColor
            glassOpacity = 0.6f
            dashboardGlow = 4.dp
            companionEmphasis = 0.6f
            guidanceEmphasis = 0.6f
            conductorEmphasis = 0.7f
            backgroundIntensity = 0.4f
        }
        AmbientMode.CALM -> {
            glassTint = baseTheme.surfaceColor
            glassOpacity = 0.4f
            dashboardGlow = 0.dp
            companionEmphasis = 0.3f
            guidanceEmphasis = 0.3f
            conductorEmphasis = 0.5f
            backgroundIntensity = 0.2f
        }
    }
    
    return AmbientContext(
        mode = mode,
        glassTint = glassTint,
        glassOpacity = glassOpacity,
        dashboardGlow = dashboardGlow,
        companionEmphasis = companionEmphasis,
        guidanceEmphasis = guidanceEmphasis,
        conductorEmphasis = conductorEmphasis,
        backgroundIntensity = backgroundIntensity
    )
}
