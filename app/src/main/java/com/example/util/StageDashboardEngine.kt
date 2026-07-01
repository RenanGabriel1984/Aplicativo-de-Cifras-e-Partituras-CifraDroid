package com.example.util

object StageDashboardEngine {
    fun produceState(
        structure: MusicalStructure?,
        timeline: MusicalTimeline,
        readingContext: ReadingContext?,
        session: PerformanceSession?,
        currentPage: Int,
        markers: List<ScoreMarker>,
        repertoireTotal: Int
    ): DashboardState {
        val currentSectionObj = structure?.sections?.firstOrNull { currentPage in it.startPage..it.endPage }
        val currentSection = currentSectionObj?.name ?: "Seção atual"
        
        val nextSectionObj = structure?.sections?.firstOrNull { it.startPage > currentPage }
        val nextSection = nextSectionObj?.name

        val upcomingMarker = markers.firstOrNull { marker ->
            marker.page > currentPage && !timeline.visitedMarkers.contains(marker.id)
        }

        val nextInstruction = upcomingMarker?.type?.name ?: readingContext?.message
        val pagesUntilInstruction = upcomingMarker?.let { it.page - currentPage }

        return DashboardState(
            currentSong = session?.currentSong ?: "Música avulsa",
            currentSection = currentSection,
            nextSection = nextSection,
            currentPass = timeline.currentPass,
            nextInstruction = nextInstruction,
            pagesUntilInstruction = pagesUntilInstruction,
            repertoireProgress = session?.songsPlayed ?: 0,
            repertoireTotal = repertoireTotal,
            elapsedTime = session?.elapsedTime ?: 0L,
            sessionActive = session?.isRunning == true
        )
    }
}
