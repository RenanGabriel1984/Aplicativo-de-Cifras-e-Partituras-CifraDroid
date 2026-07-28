package com.example.domain.workspace

enum class WorkspaceSectionType {
    GENERAL,
    LITURGY,
    REPERTOIRE,
    RECENT,
    FAVORITES,
    CUSTOM
}

data class WorkspaceSection(
    val id: String,
    val title: String,
    val icon: String,
    val type: WorkspaceSectionType,
    val collections: List<WorkspaceCollection>
)
