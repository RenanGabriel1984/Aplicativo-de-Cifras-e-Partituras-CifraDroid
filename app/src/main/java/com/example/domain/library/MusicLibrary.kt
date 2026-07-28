package com.example.domain.library

import com.example.domain.workspace.WorkspaceStatistics

data class MusicLibrary(
    val id: String,
    val title: String,
    val collections: List<LibraryCollection>,
    val filters: List<LibraryFilter>,
    val statistics: WorkspaceStatistics,
    val createdAt: Long,
    val updatedAt: Long
)
