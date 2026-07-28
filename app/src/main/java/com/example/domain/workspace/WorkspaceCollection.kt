package com.example.domain.workspace

import com.example.domain.models.SongDocument

data class WorkspaceCollection(
    val id: String,
    val title: String,
    val color: String,
    val icon: String,
    val songDocuments: List<SongDocument>
)
