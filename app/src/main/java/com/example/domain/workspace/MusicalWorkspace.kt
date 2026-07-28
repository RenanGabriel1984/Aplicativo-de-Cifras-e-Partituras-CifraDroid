package com.example.domain.workspace

data class MusicalWorkspace(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val color: String,
    val sections: List<WorkspaceSection>,
    val statistics: WorkspaceStatistics,
    val shortcuts: List<WorkspaceShortcut>,
    val createdAt: Long,
    val updatedAt: Long
)
