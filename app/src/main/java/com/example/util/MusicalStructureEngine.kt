package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object MusicalStructureEngine {
    suspend fun analyzeStructure(
        pageCount: Int,
        markers: List<ScoreMarker>,
        relationships: List<ScoreRelationship>,
        timeline: MusicalTimeline? = null
    ): MusicalStructure = withContext(Dispatchers.IO) {
        if (pageCount == 0) return@withContext MusicalStructure(emptyList())

        val sections = mutableListOf<StructureSection>()
        var currentSectionStartPage = 0
        var currentSectionType = StructureType.INTRO
        
        // Very basic heuristic based on markers
        val sortedMarkers = markers.sortedBy { it.page }

        for (i in 0 until pageCount) {
            val markersInPage = sortedMarkers.filter { it.page == i }
            
            // If we find markers, we might want to split or define the section
            if (markersInPage.isNotEmpty()) {
                val hasSegno = markersInPage.any { it.type == ScoreMarkerType.SEGNO }
                val hasToCoda = markersInPage.any { it.type == ScoreMarkerType.TO_CODA }
                val hasCoda = markersInPage.any { it.type == ScoreMarkerType.CODA }
                val hasFine = markersInPage.any { it.type == ScoreMarkerType.FINE || it.type == ScoreMarkerType.AL_FINE }
                val hasDaCapo = markersInPage.any { it.type == ScoreMarkerType.DA_CAPO }
                val hasDalSegno = markersInPage.any { it.type == ScoreMarkerType.DAL_SEGNO }
                
                if (hasSegno) {
                    if (currentSectionStartPage < i) {
                        sections.add(createSection(currentSectionStartPage, i - 1, currentSectionType, markers))
                    }
                    currentSectionStartPage = i
                    currentSectionType = StructureType.CHORUS // Segno is often chorus or main theme
                } else if (hasToCoda || hasDaCapo || hasDalSegno) {
                    // Ends a section
                    sections.add(createSection(currentSectionStartPage, i, currentSectionType, markers))
                    currentSectionStartPage = i + 1
                    currentSectionType = StructureType.VERSE // fallback for next
                } else if (hasCoda) {
                    if (currentSectionStartPage < i) {
                        sections.add(createSection(currentSectionStartPage, i - 1, currentSectionType, markers))
                    }
                    currentSectionStartPage = i
                    currentSectionType = StructureType.CODA
                } else if (hasFine) {
                    sections.add(createSection(currentSectionStartPage, i, StructureType.ENDING, markers))
                    currentSectionStartPage = i + 1
                    currentSectionType = StructureType.UNKNOWN
                }
            }
        }
        
        if (currentSectionStartPage < pageCount) {
            sections.add(createSection(currentSectionStartPage, pageCount - 1, currentSectionType, markers))
        }

        return@withContext MusicalStructure(sections.filter { it.startPage <= it.endPage })
    }

    private fun createSection(start: Int, end: Int, type: StructureType, allMarkers: List<ScoreMarker>): StructureSection {
        val markersInSection = allMarkers.filter { it.page in start..end }
        val name = when (type) {
            StructureType.INTRO -> "Intro"
            StructureType.VERSE -> "Verso"
            StructureType.CHORUS -> "Refrão"
            StructureType.BRIDGE -> "Ponte"
            StructureType.SOLO -> "Solo"
            StructureType.CODA -> "Coda"
            StructureType.ENDING -> "Final"
            StructureType.UNKNOWN -> "Seção"
        }
        return StructureSection(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            startPage = start,
            endPage = end,
            markers = markersInSection
        )
    }
}
