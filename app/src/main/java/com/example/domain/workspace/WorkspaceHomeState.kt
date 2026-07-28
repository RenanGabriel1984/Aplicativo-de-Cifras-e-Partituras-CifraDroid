package com.example.domain.workspace

import com.example.domain.models.SongDocument

data class WorkspaceHomeState(
    val currentWorkspace: MusicalWorkspace,
    val todaySummary: String,
    val continueReading: SongDocument?,
    val favorites: List<SongDocument>,
    val recentSongs: List<SongDocument>,
    val repertoires: List<WorkspaceCollection>,
    val shortcuts: List<WorkspaceShortcut>,
    val statistics: WorkspaceStatistics
)
