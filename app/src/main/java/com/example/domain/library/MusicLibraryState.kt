package com.example.domain.library

import com.example.domain.models.SongDocument
import com.example.domain.workspace.WorkspaceStatistics

data class MusicLibraryState(
    val collections: List<LibraryCollection>,
    val currentFilter: LibraryFilter?,
    val currentSort: LibrarySort,
    val documents: List<SongDocument>,
    val statistics: WorkspaceStatistics
)
