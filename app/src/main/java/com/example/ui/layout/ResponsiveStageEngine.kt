package com.example.ui.layout

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.ui.screens.HudState

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.getValue
import com.example.ui.theme.AppMotion

@Composable
fun rememberResponsiveStageEngine(): ResponsiveStageState {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isFoldable = false // In a real app we'd use WindowMetricsCalculator or similar, but simplify for now
    
    val layoutType = when {
        screenWidthDp >= StageBreakpoint.DESKTOP -> StageLayoutType.DESKTOP_STAGE
        screenWidthDp >= StageBreakpoint.EXPANDED -> if (isLandscape) StageLayoutType.EXTERNAL_DISPLAY else StageLayoutType.TABLET_LANDSCAPE
        screenWidthDp >= StageBreakpoint.MEDIUM -> if (isLandscape) StageLayoutType.TABLET_LANDSCAPE else StageLayoutType.TABLET
        isFoldable -> StageLayoutType.FOLDABLE
        isLandscape -> StageLayoutType.PHONE_LANDSCAPE
        else -> StageLayoutType.PHONE
    }

    val targetState = remember(layoutType, screenWidthDp) {
        when (layoutType) {
            StageLayoutType.PHONE -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = 600.dp,
                conductorHeight = 80.dp,
                hudMode = HudState.MINIMAL,
                compactMode = true,
                showSidePanels = false,
                showConductor = true,
                showCompanion = false,
                showGuidance = true,
                showTimeline = false
            )
            StageLayoutType.PHONE_LANDSCAPE -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = 400.dp,
                conductorHeight = 60.dp,
                hudMode = HudState.MINIMAL,
                compactMode = true,
                showSidePanels = false,
                showConductor = true,
                showCompanion = false,
                showGuidance = true,
                showTimeline = true
            )
            StageLayoutType.TABLET -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = 450.dp,
                conductorHeight = 120.dp,
                hudMode = HudState.EXPANDED,
                compactMode = false,
                showSidePanels = true,
                showConductor = true,
                showCompanion = true,
                showGuidance = true,
                showTimeline = true
            )
            StageLayoutType.TABLET_LANDSCAPE -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = 400.dp,
                conductorHeight = 100.dp,
                hudMode = HudState.EXPANDED,
                compactMode = false,
                showSidePanels = true,
                showConductor = true,
                showCompanion = true,
                showGuidance = true,
                showTimeline = true
            )
            StageLayoutType.FOLDABLE -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = screenWidthDp / 2,
                conductorHeight = 100.dp,
                hudMode = HudState.EXPANDED,
                compactMode = false,
                showSidePanels = true,
                showConductor = true,
                showCompanion = true,
                showGuidance = true,
                showTimeline = true
            )
            StageLayoutType.EXTERNAL_DISPLAY -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = 500.dp,
                conductorHeight = 150.dp,
                hudMode = HudState.PERFORMANCE,
                compactMode = false,
                showSidePanels = true,
                showConductor = true,
                showCompanion = true,
                showGuidance = true,
                showTimeline = true
            )
            StageLayoutType.DESKTOP_STAGE -> ResponsiveStageState(
                layout = layoutType,
                dashboardWidth = 600.dp,
                conductorHeight = 150.dp,
                hudMode = HudState.PERFORMANCE,
                compactMode = false,
                showSidePanels = true,
                showConductor = true,
                showCompanion = true,
                showGuidance = true,
                showTimeline = true
            )
        }
    }

    val animatedDashboardWidth by animateDpAsState(
        targetValue = targetState.dashboardWidth,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "dashboardWidth"
    )
    val animatedConductorHeight by animateDpAsState(
        targetValue = targetState.conductorHeight,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "conductorHeight"
    )

    return targetState.copy(
        dashboardWidth = animatedDashboardWidth,
        conductorHeight = animatedConductorHeight
    )
}
