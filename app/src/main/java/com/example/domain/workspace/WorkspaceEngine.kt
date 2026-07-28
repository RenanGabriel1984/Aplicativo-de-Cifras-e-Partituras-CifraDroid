package com.example.domain.workspace

import java.util.UUID

object WorkspaceEngine {

    fun createWorkspace(title: String, description: String, icon: String, color: String): MusicalWorkspace {
        val now = System.currentTimeMillis()
        return MusicalWorkspace(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            icon = icon,
            color = color,
            sections = emptyList(),
            statistics = WorkspaceStatistics(0, 0, 0, 0, 0f, now),
            shortcuts = emptyList(),
            createdAt = now,
            updatedAt = now
        )
    }

    fun duplicateWorkspace(workspace: MusicalWorkspace): MusicalWorkspace {
        val now = System.currentTimeMillis()
        return workspace.copy(
            id = UUID.randomUUID().toString(),
            title = "${workspace.title} (Copy)",
            createdAt = now,
            updatedAt = now
        )
    }

    fun renameWorkspace(workspace: MusicalWorkspace, newTitle: String): MusicalWorkspace {
        return workspace.copy(
            title = newTitle,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun addCollection(workspace: MusicalWorkspace, sectionId: String, collection: WorkspaceCollection): MusicalWorkspace {
        val newSections = workspace.sections.map { section ->
            if (section.id == sectionId) {
                section.copy(collections = section.collections + collection)
            } else {
                section
            }
        }
        return workspace.copy(
            sections = newSections,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun removeCollection(workspace: MusicalWorkspace, sectionId: String, collectionId: String): MusicalWorkspace {
        val newSections = workspace.sections.map { section ->
            if (section.id == sectionId) {
                section.copy(collections = section.collections.filter { it.id != collectionId })
            } else {
                section
            }
        }
        return workspace.copy(
            sections = newSections,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun reorderCollections(workspace: MusicalWorkspace, sectionId: String, newOrderIds: List<String>): MusicalWorkspace {
        val newSections = workspace.sections.map { section ->
            if (section.id == sectionId) {
                val collectionMap = section.collections.associateBy { it.id }
                val newCollections = newOrderIds.mapNotNull { collectionMap[it] }
                val remainingCollections = section.collections.filter { it.id !in newOrderIds }
                section.copy(collections = newCollections + remainingCollections)
            } else {
                section
            }
        }
        return workspace.copy(
            sections = newSections,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun calculateStatistics(workspace: MusicalWorkspace): MusicalWorkspace {
        var totalSongs = 0
        var repertoires = 0
        
        workspace.sections.forEach { section ->
            if (section.type == WorkspaceSectionType.REPERTOIRE) {
                repertoires += section.collections.size
            }
            section.collections.forEach { collection ->
                totalSongs += collection.songDocuments.size
            }
        }

        return workspace.copy(
            statistics = workspace.statistics.copy(
                totalSongs = totalSongs,
                repertoires = repertoires,
                lastAccess = System.currentTimeMillis()
            ),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun generateDefaultWorkspace(): MusicalWorkspace {
        val now = System.currentTimeMillis()
        val defaultCollection = WorkspaceCollection(
            id = UUID.randomUUID().toString(),
            title = "Músicas",
            color = "#4CAF50",
            icon = "music_note",
            songDocuments = emptyList()
        )
        
        val defaultSection = WorkspaceSection(
            id = UUID.randomUUID().toString(),
            title = "Geral",
            icon = "folder",
            type = WorkspaceSectionType.GENERAL,
            collections = listOf(defaultCollection)
        )

        return MusicalWorkspace(
            id = UUID.randomUUID().toString(),
            title = "Meu Workspace",
            description = "Workspace Principal",
            icon = "home",
            color = "#2196F3",
            sections = listOf(defaultSection),
            statistics = WorkspaceStatistics(0, 0, 0, 0, 0f, now),
            shortcuts = emptyList(),
            createdAt = now,
            updatedAt = now
        )
    }
}
