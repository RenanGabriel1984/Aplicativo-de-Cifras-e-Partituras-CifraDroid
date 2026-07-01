package com.example.util

object FlowModeEngine {
    fun produceContext(
        session: PerformanceSession?,
        dashboardState: DashboardState,
        timeline: MusicalTimeline,
        currentPage: Int,
        isScrollInProgress: Boolean,
        focusMode: Boolean,
        autoCue: Boolean
    ): FlowContext {
        val isSecondPassOrMore = timeline.currentPass != MusicalPass.FIRST_PASS

        return when {
            isScrollInProgress -> FlowContext(
                state = FlowState.TRANSITION,
                showHud = false,
                opacity = 0.5f,
                allowGestures = false,
                showMarkers = false,
                reduceAnimations = true,
                cueIntensity = CueLevel.SUBTLE
            )
            focusMode -> FlowContext(
                state = FlowState.FOCUS,
                showHud = true, // We might want to show minimal HUD or Dashboard
                opacity = 0.20f,
                allowGestures = true,
                showMarkers = false,
                reduceAnimations = false,
                cueIntensity = CueLevel.STRONG // Focus allows HIGH alerts
            )
            isSecondPassOrMore && session?.isRunning == true -> FlowContext(
                state = FlowState.IMMERSION,
                showHud = true,
                opacity = 0.8f,
                allowGestures = true,
                showMarkers = true,
                reduceAnimations = false,
                cueIntensity = CueLevel.STRONG
            )
            session?.isRunning == true -> FlowContext(
                state = FlowState.PERFORMANCE,
                showHud = true,
                opacity = 1.0f,
                allowGestures = true,
                showMarkers = true,
                reduceAnimations = false,
                cueIntensity = if (autoCue) CueLevel.NORMAL else CueLevel.SUBTLE
            )
            else -> FlowContext(
                state = FlowState.READING,
                showHud = true,
                opacity = 1.0f,
                allowGestures = true,
                showMarkers = true,
                reduceAnimations = false,
                cueIntensity = CueLevel.NORMAL
            )
        }
    }
}
