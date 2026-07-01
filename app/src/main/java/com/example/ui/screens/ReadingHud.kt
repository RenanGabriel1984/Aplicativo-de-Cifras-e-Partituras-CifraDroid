package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.delay
import com.example.util.MusicalStructure
import com.example.util.MusicalTimeline
import com.example.util.ScoreRelationship
import com.example.util.ScoreMarker
import com.example.util.ScoreMarkerType
import com.example.util.RelationshipType
import com.example.util.MusicalPass

enum class HudState {
    FOCUS,
    PERFORMANCE,
    MINIMAL,
    EXPANDED
}

@Composable
fun ReadingHud(
    currentPage: Int,
    pageCount: Int,
    musicalStructure: MusicalStructure,
    musicalTimeline: MusicalTimeline,
    scoreRelationships: List<ScoreRelationship>,
    detectedMarkers: List<ScoreMarker>,
    isPerformanceMode: Boolean,
    isFocusMode: Boolean,
    isScrollInProgress: Boolean,
    readingContext: com.example.util.ReadingContext
) {
    var isIdle by remember { mutableStateOf(true) }
    
    LaunchedEffect(isScrollInProgress, currentPage) {
        if (isScrollInProgress) {
            isIdle = false
        } else {
            delay(3000)
            isIdle = true
        }
    }

    val hudState by remember(isFocusMode, isPerformanceMode, isIdle) {
        derivedStateOf {
            when {
                isFocusMode -> HudState.FOCUS
                isPerformanceMode -> HudState.PERFORMANCE
                !isIdle -> HudState.MINIMAL
                else -> HudState.EXPANDED
            }
        }
    }

    val currentSection = remember(currentPage, musicalStructure) {
        musicalStructure.sections.firstOrNull { currentPage in it.startPage..it.endPage }
    }

    val nextSection = remember(currentPage, musicalStructure) {
        musicalStructure.sections.firstOrNull { it.startPage > currentPage }
    }

    val upcomingMarker = remember(currentPage, detectedMarkers, musicalTimeline) {
        val nextPages = (currentPage + 1)..(currentPage + 3)
        detectedMarkers.firstOrNull { marker ->
            marker.page in nextPages && 
            marker.type in listOf(
                ScoreMarkerType.DAL_SEGNO, 
                ScoreMarkerType.DA_CAPO, 
                ScoreMarkerType.TO_CODA, 
                ScoreMarkerType.FINE, 
                ScoreMarkerType.AL_FINE, 
                ScoreMarkerType.SEGNO, 
                ScoreMarkerType.CODA
            ) &&
            !musicalTimeline.visitedMarkers.contains(marker.id)
        }
    }

    AnimatedVisibility(
        visible = hudState != HudState.FOCUS,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it }
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(horizontal = if (hudState == HudState.MINIMAL) 32.dp else 16.dp, vertical = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (hudState == HudState.MINIMAL) 0.6f else 0.85f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (hudState == HudState.MINIMAL) 8.dp else 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Structure & Performance
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedVisibility(visible = hudState == HudState.PERFORMANCE || hudState == HudState.EXPANDED) {
                            if (isPerformanceMode) {
                                Text(
                                    text = "🎵 Performance",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        AnimatedContent(
                            targetState = currentSection?.name ?: "SEÇÃO DESCONHECIDA",
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                            label = "section_name"
                        ) { name ->
                            Text(
                                text = name.uppercase(),
                                style = if (hudState == HudState.MINIMAL) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(visible = hudState == HudState.EXPANDED || hudState == HudState.PERFORMANCE) {
                            if (nextSection != null) {
                                Text(
                                    text = "↓ Próximo: ${nextSection.name.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    // Center side: Context Message
                    AnimatedVisibility(
                        visible = (hudState == HudState.EXPANDED || hudState == HudState.PERFORMANCE) && readingContext.message != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = readingContext,
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                            label = "context_message"
                        ) { context ->
                            val textColor = when(context.urgency) {
                                com.example.util.ContextUrgency.LOW -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                com.example.util.ContextUrgency.MEDIUM -> Color(0xFFFFBF00).copy(alpha = 0.8f)
                                com.example.util.ContextUrgency.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            }
                            Text(
                                text = context.message ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Right side: Pass & Upcoming relationships
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = musicalTimeline.currentPass,
                            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                            label = "pass_name"
                        ) { pass ->
                            val passName = when(pass) {
                                MusicalPass.FIRST_PASS -> "1ª Passagem"
                                MusicalPass.SECOND_PASS -> "2ª Passagem"
                                MusicalPass.THIRD_PASS -> "3ª Passagem"
                                MusicalPass.FINAL_PASS -> "Última Passagem"
                            }
                            Text(
                                text = passName,
                                style = if (hudState == HudState.MINIMAL) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = hudState == HudState.EXPANDED || hudState == HudState.PERFORMANCE) {
                            AnimatedContent(
                                targetState = Pair(upcomingMarker, musicalTimeline.timelineFinished),
                                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                                label = "upcoming_marker"
                            ) { (marker, finished) ->
                                if (marker != null && !finished) {
                                    val upcomingType = when (marker.type) {
                                        ScoreMarkerType.DA_CAPO -> "D.C."
                                        ScoreMarkerType.DAL_SEGNO -> "D.S."
                                        ScoreMarkerType.TO_CODA -> "To Coda"
                                        ScoreMarkerType.FINE, ScoreMarkerType.AL_FINE -> "Fine"
                                        ScoreMarkerType.SEGNO -> "Segno"
                                        ScoreMarkerType.CODA -> "Coda"
                                        else -> "Salto"
                                    }
                                    Text(
                                        text = "↓ $upcomingType",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else if (finished) {
                                    Text(
                                        text = "Fim",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(16.dp)) // Maintain layout height if empty
                                }
                            }
                        }
                    }
                }

                // Progress Bar
                Spacer(modifier = Modifier.height(if (hudState == HudState.MINIMAL) 4.dp else 8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(if (hudState == HudState.MINIMAL) 1.dp else 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val sections = musicalStructure.sections
                    if (sections.isEmpty()) {
                        val progress = if (pageCount > 0) ((currentPage + 1).toFloat() / pageCount.toFloat()) else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    } else {
                        sections.forEach { section ->
                            val isCurrent = currentPage in section.startPage..section.endPage
                            val isPast = currentPage > section.endPage
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary 
                                                else if (isPast) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
