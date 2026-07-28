package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppSpacing
import com.example.util.ConductorState
import com.example.util.MusicalSemanticBlock
import com.example.util.MusicalStructure
import com.example.util.MusicalTimeline

@Composable
fun ConductorTrack(
    conductorState: ConductorState,
    musicalStructure: MusicalStructure? = null,
    musicalSemantics: List<MusicalSemanticBlock> = emptyList(),
    timeline: MusicalTimeline? = null,
    modifier: Modifier = Modifier
) {
    val stageState = com.example.ui.layout.rememberResponsiveStageEngine()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.MD)
    ) {
        // Track segments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(stageState.conductorHeight),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.XS),
            verticalAlignment = Alignment.CenterVertically
        ) {
            conductorState.sections.forEachIndexed { index, section ->
                val weight = (section.endPage - section.startPage + 1).toFloat().coerceAtLeast(1f)
                ConductorSegment(
                    type = section.type,
                    active = section.active,
                    completed = section.completed,
                    modifier = Modifier.weight(weight)
                )
            }
        }

        // Markers
        val activeSection = conductorState.sections.getOrNull(conductorState.currentSection)
        val structureSection = musicalStructure?.sections?.find { it.id == activeSection?.id }
        
        AnimatedContent(
            targetState = structureSection?.markers,
            label = "MarkersAnimation"
        ) { markers ->
            if (markers != null && markers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.SM)
                ) {
                    markers.forEach { marker ->
                        Crossfade(targetState = marker, label = "MarkerCrossfade") { currentMarker ->
                            ConductorMarker(type = currentMarker.type, text = currentMarker.text)
                        }
                    }
                }
            }
        }

        // Progress
        val currentPage = timeline?.currentPage ?: activeSection?.startPage ?: 1
        val totalPages = conductorState.sections.maxOfOrNull { it.endPage } ?: 1
        
        ConductorProgress(
            progress = conductorState.progress,
            currentPage = currentPage,
            totalPages = totalPages
        )
    }
}
