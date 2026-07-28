package com.example.domain.library

import com.example.domain.models.SongDocument
import com.example.domain.workspace.WorkspaceStatistics
import java.util.UUID

object MusicLibraryEngine {

    fun createLibrary(title: String): MusicLibrary {
        val now = System.currentTimeMillis()
        return MusicLibrary(
            id = UUID.randomUUID().toString(),
            title = title,
            collections = generateDefaultCollections(),
            filters = emptyList(),
            statistics = WorkspaceStatistics(0, 0, 0, 0, 0f, now),
            createdAt = now,
            updatedAt = now
        )
    }

    fun buildCollections(documents: List<SongDocument>): List<LibraryCollection> {
        // Will properly map documents to collections in the future
        return generateDefaultCollections()
    }

    fun sortLibrary(documents: List<SongDocument>, sort: LibrarySort): List<SongDocument> {
        return when (sort) {
            LibrarySort.TITLE -> documents.sortedBy { it.metadata.title }
            LibrarySort.ARTIST -> documents.sortedBy { it.metadata.artist }
            else -> documents
        }
    }

    fun filterLibrary(documents: List<SongDocument>, filter: LibraryFilter?): List<SongDocument> {
        if (filter == null) return documents
        return documents
    }

    fun calculateStatistics(documents: List<SongDocument>, repertoires: Int = 0): WorkspaceStatistics {
        return WorkspaceStatistics(
            totalSongs = documents.size,
            favorites = 0, // Placeholder
            recentSongs = 0, // Placeholder
            repertoires = repertoires,
            hoursStudied = 0f,
            lastAccess = System.currentTimeMillis()
        )
    }

    fun generateDefaultCollections(): List<LibraryCollection> {
        return listOf(
            LibraryCollection(UUID.randomUUID().toString(), "Favoritas", "favorite", "#E91E63", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Mais Tocadas", "trending_up", "#FF9800", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Recentes", "schedule", "#2196F3", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Importadas", "download", "#4CAF50", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Autor", "person", "#9C27B0", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Categorias", "category", "#00BCD4", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Liturgia", "church", "#795548", emptyList()),
            LibraryCollection(UUID.randomUUID().toString(), "Artistas", "mic", "#607D8B", emptyList())
        )
    }
}
