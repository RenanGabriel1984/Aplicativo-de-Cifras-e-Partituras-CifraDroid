package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.util.FlowContext
import com.example.util.FlowState
import com.example.util.MusicalIntent
import com.example.util.MusicalIntentType

@Composable
fun rememberPerformanceMinimalEngine(
    flowContext: FlowContext,
    musicalIntent: MusicalIntent?
): PerformanceMinimalState {
    return remember(flowContext.state, musicalIntent?.type) {
        val isChorus = musicalIntent?.type == MusicalIntentType.CHORUS_PEAK
        val isEnding = musicalIntent?.type == MusicalIntentType.ENDING || musicalIntent?.type == MusicalIntentType.FINAL_CODA
        
        when {
            isEnding -> PerformanceMinimalState(
                dashboard = MinimalVisibility.REDUCED,
                companion = MinimalVisibility.VISIBLE,
                guidance = MinimalVisibility.HIDDEN,
                conductor = MinimalVisibility.REDUCED,
                hud = MinimalVisibility.REDUCED,
                session = MinimalVisibility.REDUCED,
                timeline = MinimalVisibility.HIDDEN
            )
            isChorus -> PerformanceMinimalState(
                dashboard = MinimalVisibility.REDUCED,
                companion = MinimalVisibility.VISIBLE,
                guidance = MinimalVisibility.VISIBLE,
                conductor = MinimalVisibility.REDUCED,
                hud = MinimalVisibility.REDUCED,
                session = MinimalVisibility.REDUCED,
                timeline = MinimalVisibility.HIDDEN
            )
            flowContext.state == FlowState.FOCUS -> PerformanceMinimalState(
                dashboard = MinimalVisibility.REDUCED,
                companion = MinimalVisibility.VISIBLE,
                guidance = MinimalVisibility.HIDDEN,
                conductor = MinimalVisibility.REDUCED,
                hud = MinimalVisibility.REDUCED,
                session = MinimalVisibility.REDUCED,
                timeline = MinimalVisibility.HIDDEN
            )
            flowContext.state == FlowState.PERFORMANCE -> PerformanceMinimalState(
                dashboard = MinimalVisibility.VISIBLE,
                companion = MinimalVisibility.VISIBLE,
                guidance = MinimalVisibility.VISIBLE,
                conductor = MinimalVisibility.VISIBLE,
                hud = MinimalVisibility.VISIBLE,
                session = MinimalVisibility.VISIBLE,
                timeline = MinimalVisibility.VISIBLE
            )
            flowContext.state == FlowState.IMMERSION -> PerformanceMinimalState(
                dashboard = MinimalVisibility.REDUCED,
                companion = MinimalVisibility.VISIBLE,
                guidance = MinimalVisibility.REDUCED,
                conductor = MinimalVisibility.VISIBLE,
                hud = MinimalVisibility.VISIBLE,
                session = MinimalVisibility.VISIBLE,
                timeline = MinimalVisibility.REDUCED
            )
            else -> PerformanceMinimalState(
                dashboard = MinimalVisibility.VISIBLE,
                companion = MinimalVisibility.VISIBLE,
                guidance = MinimalVisibility.VISIBLE,
                conductor = MinimalVisibility.VISIBLE,
                hud = MinimalVisibility.VISIBLE,
                session = MinimalVisibility.VISIBLE,
                timeline = MinimalVisibility.VISIBLE
            )
        }
    }
}
