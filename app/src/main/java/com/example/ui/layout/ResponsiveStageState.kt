package com.example.ui.layout

import androidx.compose.ui.unit.Dp
import com.example.ui.screens.HudState

data class ResponsiveStageState(
    val layout: StageLayoutType,
    val dashboardWidth: Dp,
    val conductorHeight: Dp,
    val hudMode: HudState,
    val compactMode: Boolean,
    val showSidePanels: Boolean,
    val showConductor: Boolean,
    val showCompanion: Boolean,
    val showGuidance: Boolean,
    val showTimeline: Boolean
)
