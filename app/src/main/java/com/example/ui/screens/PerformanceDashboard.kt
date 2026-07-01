package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import com.example.util.DashboardState
import com.example.util.MusicalPass

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
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.sessionActive && flowContext.showHud,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        val displayMode = when (flowContext.state) {
            com.example.util.FlowState.TRANSITION -> DashboardDisplayMode.COMPACT
            com.example.util.FlowState.FOCUS -> DashboardDisplayMode.FOCUS
            com.example.util.FlowState.PERFORMANCE -> DashboardDisplayMode.FOCUS
            com.example.util.FlowState.IMMERSION -> DashboardDisplayMode.FOCUS
            else -> DashboardDisplayMode.EXPANDED
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f * flowContext.opacity),
                    shadowElevation = if (displayMode == DashboardDisplayMode.FOCUS) 0.dp else 4.dp
                ) {
                    Crossfade(targetState = displayMode, label = "dashboard_mode", animationSpec = if (flowContext.reduceAnimations) snap() else tween()) { mode ->
                        when (mode) {
                            DashboardDisplayMode.COMPACT -> CompactDashboard(presentation, flowContext.cueIntensity, semanticState)
                            DashboardDisplayMode.EXPANDED -> ExpandedDashboard(presentation, flowContext.cueIntensity, semanticState)
                            DashboardDisplayMode.FOCUS -> FocusDashboard(presentation, flowContext.cueIntensity, semanticState)
                        }
                    }
                }

                AnimatedContent(
                    targetState = adaptiveGuidance,
                    label = "adaptive_guidance",
                    transitionSpec = {
                        fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                    }
                ) { guidance ->
                    if (guidance != null) {
                        val backgroundColor = when (guidance.level) {
                            com.example.util.GuidanceLevel.SUBTLE -> MaterialTheme.colorScheme.surfaceVariant
                            com.example.util.GuidanceLevel.NORMAL -> MaterialTheme.colorScheme.primary
                            com.example.util.GuidanceLevel.IMPORTANT -> Color(0xFFFFB300) // Amber
                            com.example.util.GuidanceLevel.CRITICAL -> MaterialTheme.colorScheme.error
                        }
                        val contentColor = when (guidance.level) {
                            com.example.util.GuidanceLevel.SUBTLE -> MaterialTheme.colorScheme.onSurfaceVariant
                            com.example.util.GuidanceLevel.NORMAL -> MaterialTheme.colorScheme.onPrimary
                            com.example.util.GuidanceLevel.IMPORTANT -> Color.Black // onAmber
                            com.example.util.GuidanceLevel.CRITICAL -> MaterialTheme.colorScheme.onError
                        }

                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .widthIn(max = 600.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = backgroundColor.copy(alpha = 0.9f * flowContext.opacity),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${guidance.title} • ${guidance.message}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(0.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = semanticState?.title ?: presentation.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (cueIntensity == CueLevel.NONE && presentation.cue != null) (semanticState?.subtitle ?: presentation.subtitle) else (presentation.cue ?: (semanticState?.subtitle ?: presentation.subtitle)),
            style = MaterialTheme.typography.labelSmall,
            color = if (cueIntensity == CueLevel.STRONG) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExpandedDashboard(presentation: DashboardPresentation, cueIntensity: CueLevel, semanticState: com.example.util.SemanticReadingState?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = semanticState?.title ?: presentation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (presentation.badge != null) {
                Text(
                    text = presentation.badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = semanticState?.subtitle ?: presentation.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        if (presentation.cue != null && cueIntensity != CueLevel.NONE) {
            val isHighEmphasis = presentation.emphasis == PresentationPriority.HIGH || cueIntensity == CueLevel.STRONG || semanticState?.priority == PresentationPriority.HIGH
            Surface(
                color = if (isHighEmphasis) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = presentation.cue,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isHighEmphasis) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FocusDashboard(presentation: DashboardPresentation, cueIntensity: CueLevel, semanticState: com.example.util.SemanticReadingState?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val texts = if (cueIntensity == CueLevel.NONE) {
            listOfNotNull(semanticState?.title ?: presentation.title, semanticState?.subtitle ?: presentation.subtitle, presentation.badge)
        } else {
            listOfNotNull(semanticState?.title ?: presentation.title, semanticState?.subtitle ?: presentation.subtitle, presentation.badge, presentation.cue)
        }
        val combinedText = texts.filter { it.isNotBlank() }.joinToString(" • ")
        
        Text(
            text = combinedText,
            style = MaterialTheme.typography.labelSmall,
            color = if (cueIntensity == CueLevel.STRONG || semanticState?.priority == PresentationPriority.HIGH) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
