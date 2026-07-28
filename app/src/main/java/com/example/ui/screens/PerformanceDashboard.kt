package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.DashboardState
import com.example.util.DashboardPresentation
import com.example.util.PresentationPriority
import com.example.util.CueLevel

@Composable
fun PerformanceDashboard(
    state: DashboardState,
    presentation: DashboardPresentation,
    flowContext: com.example.util.FlowContext,
    semanticState: com.example.util.SemanticReadingState? = null,
    adaptiveGuidance: com.example.util.AdaptiveGuidance? = null,
    musicalIntent: com.example.util.MusicalIntent? = null,
    conductorState: com.example.util.ConductorState? = null,
    performanceIntelligence: com.example.util.PerformanceIntelligence? = null,
    companionState: com.example.util.MusicalCompanionMessage? = null,
    musicalStructure: com.example.util.MusicalStructure? = null,
    musicalSemantics: List<com.example.util.MusicalSemanticBlock> = emptyList(),
    timeline: com.example.util.MusicalTimeline? = null,
    immersiveContext: com.example.ui.theme.ImmersiveContext? = null,
    ambientContext: com.example.ui.theme.AmbientContext? = null,
    minimalState: com.example.ui.theme.PerformanceMinimalState? = null,
    songDocument: com.example.domain.models.SongDocument? = null,
    musicalWorkspace: com.example.domain.workspace.MusicalWorkspace? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.sessionActive && flowContext.showHud,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        StageThemeRenderer(ambientContext = ambientContext) {
            val stageState = com.example.ui.layout.rememberResponsiveStageEngine()
            
            val visualContext = rememberVisualCohesionEngine(
                flowContext = flowContext,
                responsiveState = stageState,
                guidance = adaptiveGuidance,
                companion = companionState,
                intent = musicalIntent,
                timeline = timeline
            )

            val guidanceMinimalAlpha = when (minimalState?.guidance) {
                com.example.ui.theme.MinimalVisibility.HIDDEN -> 0f
                com.example.ui.theme.MinimalVisibility.REDUCED -> 0.3f
                else -> 1f
            }
            val companionMinimalAlpha = when (minimalState?.companion) {
                com.example.ui.theme.MinimalVisibility.HIDDEN -> 0f
                com.example.ui.theme.MinimalVisibility.REDUCED -> 0.3f
                else -> 1f
            }
            val conductorMinimalAlpha = when (minimalState?.conductor) {
                com.example.ui.theme.MinimalVisibility.HIDDEN -> 0f
                com.example.ui.theme.MinimalVisibility.REDUCED -> 0.3f
                else -> 1f
            }

            val sessionMinimalAlpha = when (minimalState?.session) {
                com.example.ui.theme.MinimalVisibility.HIDDEN -> 0f
                com.example.ui.theme.MinimalVisibility.REDUCED -> 0.3f
                else -> 1f
            }
            
            val guidanceAlphaTarget = (immersiveContext?.guidanceVisibility ?: 1f) * (ambientContext?.guidanceEmphasis ?: 1f) * guidanceMinimalAlpha
            val companionAlphaTarget = (immersiveContext?.companionVisibility ?: 1f) * (ambientContext?.companionEmphasis ?: 1f) * companionMinimalAlpha
            val conductorAlphaTarget = (immersiveContext?.conductorVisibility ?: 1f) * (ambientContext?.conductorEmphasis ?: 1f) * conductorMinimalAlpha
            
            val animProfile = immersiveContext?.animationProfile ?: com.example.ui.theme.AppMotion.Smooth
            val guidanceAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = guidanceAlphaTarget, animationSpec = animProfile, label = "guidanceAlpha")
            val companionAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = companionAlphaTarget, animationSpec = animProfile, label = "companionAlpha")
            val conductorAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = conductorAlphaTarget, animationSpec = animProfile, label = "conductorAlpha")
            val sessionAlpha by androidx.compose.animation.core.animateFloatAsState(targetValue = sessionMinimalAlpha, animationSpec = animProfile, label = "sessionAlpha")

            val displayMode = when (flowContext.state) {
                com.example.util.FlowState.TRANSITION -> DashboardDisplayMode.COMPACT
                com.example.util.FlowState.FOCUS -> DashboardDisplayMode.FOCUS
                com.example.util.FlowState.PERFORMANCE -> DashboardDisplayMode.FOCUS
                com.example.util.FlowState.IMMERSION -> DashboardDisplayMode.FOCUS
                else -> if (stageState.compactMode) DashboardDisplayMode.COMPACT else DashboardDisplayMode.EXPANDED
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                DashboardContainer(
                    modifier = Modifier.widthIn(max = stageState.dashboardWidth),
                    immersiveContext = immersiveContext,
                    ambientContext = ambientContext,
                    minimalState = minimalState
                ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.MD)
                ) {
                    // CurrentSection
                    DashboardCard(modifier = Modifier.fillMaxWidth().visualCohesion(visualContext.dashboardPriority, visualContext)) {
                        Crossfade(
                            targetState = displayMode, 
                            label = "dashboard_mode", 
                            animationSpec = if (flowContext.reduceAnimations) snap() else tween()
                        ) { mode ->
                            when (mode) {
                                DashboardDisplayMode.COMPACT -> CompactDashboard(presentation, flowContext.cueIntensity, semanticState)
                                DashboardDisplayMode.EXPANDED -> ExpandedDashboard(presentation, flowContext.cueIntensity, semanticState)
                                DashboardDisplayMode.FOCUS -> FocusDashboard(presentation, flowContext.cueIntensity, semanticState)
                            }
                        }
                    }

                    // Song Document Metadata
                    if (songDocument != null) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                                    DashboardHeader(
                                        title = songDocument.metadata.title,
                                        subtitle = "Categoria: ${songDocument.metadata.category.name}",
                                        metric = "${songDocument.metadata.bpm} BPM"
                                    )
                                    DashboardDivider()
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        DashboardBadge(
                                            text = "Tom: ${songDocument.metadata.key}",
                                            badgeColor = BadgeColor.PRIMARY,
                                            isPill = false
                                        )
                                        if (songDocument.metadata.capo > 0) {
                                            DashboardBadge(
                                                text = "Capo: ${songDocument.metadata.capo}",
                                                badgeColor = BadgeColor.AMBER,
                                                isPill = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Workspace
                    if (musicalWorkspace != null) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                                    DashboardHeader(
                                        title = "Workspace",
                                        subtitle = musicalWorkspace.title,
                                        metric = ""
                                    )
                                    DashboardDivider()
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        DashboardBadge(
                                            text = "${musicalWorkspace.statistics.totalSongs} músicas",
                                            badgeColor = BadgeColor.SURFACE_VARIANT,
                                            isPill = false
                                        )
                                        DashboardBadge(
                                            text = "${musicalWorkspace.statistics.repertoires} repertórios",
                                            badgeColor = BadgeColor.SURFACE_VARIANT,
                                            isPill = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Companion
                    AnimatedContent(
                        targetState = if (stageState.showCompanion) companionState else null,
                        label = "musical_companion",
                        transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() }
                    ) { companion ->
                        if (companion != null) {
                            DashboardCard(modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = companionAlpha).visualCohesion(visualContext.companionPriority, visualContext)) {
                                val badgeColor = when (companion.tone) {
                                    com.example.util.CompanionTone.CALM -> BadgeColor.SURFACE_VARIANT
                                    com.example.util.CompanionTone.GUIDE -> BadgeColor.PRIMARY
                                    com.example.util.CompanionTone.WARNING -> BadgeColor.AMBER
                                    com.example.util.CompanionTone.CLIMAX -> BadgeColor.PRIMARY
                                    com.example.util.CompanionTone.ENDING -> BadgeColor.ERROR
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = companion.title,
                                            style = AppTypography.DashboardTitle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = companion.message,
                                            style = AppTypography.CompanionText,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DashboardBadge(
                                        text = companion.tone.name,
                                        badgeColor = badgeColor,
                                        isPill = true
                                    )
                                }
                            }
                        } else { Spacer(modifier = Modifier.height(0.dp)) }
                    }

                    // Guidance
                    AnimatedContent(
                        targetState = if (stageState.showGuidance) adaptiveGuidance else null,
                        label = "adaptive_guidance",
                        transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() }
                    ) { guidance ->
                        if (guidance != null) {
                            DashboardCard(modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = guidanceAlpha).visualCohesion(visualContext.guidancePriority, visualContext)) {
                                val badgeColor = when (guidance.level) {
                                    com.example.util.GuidanceLevel.SUBTLE -> BadgeColor.SURFACE_VARIANT
                                    com.example.util.GuidanceLevel.NORMAL -> BadgeColor.PRIMARY
                                    com.example.util.GuidanceLevel.IMPORTANT -> BadgeColor.AMBER
                                    com.example.util.GuidanceLevel.CRITICAL -> BadgeColor.ERROR
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                                    DashboardBadge(
                                        text = guidance.level.name,
                                        badgeColor = badgeColor,
                                        isPill = true
                                    )
                                    DashboardHeader(
                                        title = guidance.title,
                                        subtitle = guidance.message
                                    )
                                }
                            }
                        } else { Spacer(modifier = Modifier.height(0.dp)) }
                    }

                    // Intelligence
                    AnimatedContent(
                        targetState = performanceIntelligence,
                        label = "performance_intelligence",
                        transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() }
                    ) { intel ->
                        if (intel != null) {
                            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                                    DashboardHeader(
                                        title = intel.title,
                                        subtitle = intel.subtitle,
                                        metric = "${intel.confidence}%"
                                    )
                                    if (intel.nextEvent != null) {
                                        DashboardDivider()
                                        DashboardBadge(
                                            text = "Next: ${intel.nextEvent} (${intel.pagesAhead}p)",
                                            badgeColor = BadgeColor.SURFACE_VARIANT,
                                            isPill = false
                                        )
                                    }
                                }
                            }
                        } else { Spacer(modifier = Modifier.height(0.dp)) }
                    }

                    // Conductor
                    AnimatedVisibility(
                        visible = conductorState != null && stageState.showConductor,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        conductorState?.let { cState ->
                            DashboardCard(modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = conductorAlpha).visualCohesion(visualContext.conductorPriority, visualContext)) {
                                ConductorTrack(
                                    conductorState = cState,
                                    musicalStructure = musicalStructure,
                                    musicalSemantics = musicalSemantics,
                                    timeline = timeline,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Session
                    DashboardCard(modifier = Modifier.fillMaxWidth().graphicsLayer(alpha = sessionAlpha).visualCohesion(visualContext.sessionPriority, visualContext)) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.MD)) {
                            val progress = if (state.repertoireTotal > 0) {
                                state.repertoireProgress.toFloat() / state.repertoireTotal.toFloat()
                            } else 0f
                            DashboardHeader(
                                title = "Session",
                                subtitle = "${state.repertoireProgress} / ${state.repertoireTotal} Songs"
                            )
                            DashboardProgress(progress = progress)
                        }
                    }

                    // Intent
                    AnimatedContent(
                        targetState = musicalIntent,
                        label = "musical_intent",
                        transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() }
                    ) { intent ->
                        if (intent != null) {
                            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                                val badgeColor = when {
                                    intent.confidence >= 90 -> BadgeColor.ERROR
                                    intent.confidence >= 75 -> BadgeColor.AMBER
                                    intent.confidence >= 50 -> BadgeColor.PRIMARY
                                    else -> BadgeColor.SURFACE_VARIANT
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)) {
                                    DashboardBadge(
                                        text = "Intent",
                                        badgeColor = badgeColor,
                                        isPill = true
                                    )
                                    DashboardHeader(
                                        title = intent.title,
                                        subtitle = intent.description,
                                        metric = "${intent.confidence}%"
                                    )
                                }
                            }
                        } else { Spacer(modifier = Modifier.height(0.dp)) }
                    }
                }
            }
        }
    }
}
}

enum class DashboardDisplayMode {
    COMPACT, EXPANDED, FOCUS
}

@Composable
fun CompactDashboard(presentation: DashboardPresentation, cueIntensity: CueLevel, semanticState: com.example.util.SemanticReadingState?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = semanticState?.title ?: presentation.title,
            style = AppTypography.TitleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        val cueText = if (cueIntensity == CueLevel.NONE && presentation.cue != null) (semanticState?.subtitle ?: presentation.subtitle) else (presentation.cue ?: (semanticState?.subtitle ?: presentation.subtitle))
        Text(
            text = cueText,
            style = AppTypography.BodyMedium,
            color = if (cueIntensity == CueLevel.STRONG) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExpandedDashboard(presentation: DashboardPresentation, cueIntensity: CueLevel, semanticState: com.example.util.SemanticReadingState?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.SM)
    ) {
        DashboardHeader(
            title = semanticState?.title ?: presentation.title,
            subtitle = semanticState?.subtitle ?: presentation.subtitle,
            metric = presentation.badge
        )
        if (presentation.cue != null && cueIntensity != CueLevel.NONE) {
            DashboardDivider()
            val isHighEmphasis = presentation.emphasis == PresentationPriority.HIGH || cueIntensity == CueLevel.STRONG || semanticState?.priority == PresentationPriority.HIGH
            val badgeColor = if (isHighEmphasis) BadgeColor.ERROR else BadgeColor.SURFACE_VARIANT
            DashboardBadge(
                text = presentation.cue,
                badgeColor = badgeColor,
                isPill = false
            )
        }
    }
}

@Composable
fun FocusDashboard(presentation: DashboardPresentation, cueIntensity: CueLevel, semanticState: com.example.util.SemanticReadingState?) {
    val texts = if (cueIntensity == CueLevel.NONE) {
        listOfNotNull(semanticState?.title ?: presentation.title, semanticState?.subtitle ?: presentation.subtitle, presentation.badge)
    } else {
        listOfNotNull(semanticState?.title ?: presentation.title, semanticState?.subtitle ?: presentation.subtitle, presentation.badge, presentation.cue)
    }
    val combinedText = texts.filter { it.isNotBlank() }.joinToString(" • ")
    
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = combinedText,
            style = AppTypography.CueText,
            color = if (cueIntensity == CueLevel.STRONG || semanticState?.priority == PresentationPriority.HIGH) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
