package com.example.domain

import com.example.domain.models.SongDocument
import com.example.domain.models.SongMetadata
import com.example.domain.models.SongSection
import com.example.domain.models.SongSectionType
import java.util.UUID

object SongDocumentEngine {
    
    fun createDocument(metadata: SongMetadata): SongDocument {
        return SongDocument(
            metadata = metadata,
            sections = emptyList(),
            annotations = emptyList(),
            linkedPdf = null,
            linkedAudio = null
        )
    }

    fun duplicateDocument(document: SongDocument): SongDocument {
        val newMetadata = document.metadata.copy(
            id = UUID.randomUUID().toString(),
            title = "${document.metadata.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return document.copy(
            metadata = newMetadata,
            sections = document.sections.map { it.copy(id = UUID.randomUUID().toString()) }
        )
    }

    fun updateMetadata(document: SongDocument, metadata: SongMetadata): SongDocument {
        return document.copy(
            metadata = metadata.copy(updatedAt = System.currentTimeMillis())
        )
    }

    fun reorderSections(document: SongDocument, newOrderIds: List<String>): SongDocument {
        val sectionMap = document.sections.associateBy { it.id }
        val newSections = newOrderIds.mapIndexedNotNull { index, id ->
            sectionMap[id]?.copy(order = index)
        }
        val remainingSections = document.sections.filter { it.id !in newOrderIds }
            .mapIndexed { index, section -> section.copy(order = newSections.size + index) }
            
        return document.copy(
            sections = newSections + remainingSections,
            metadata = document.metadata.copy(updatedAt = System.currentTimeMillis())
        )
    }

    fun transposeDocument(document: SongDocument, newKey: String): SongDocument {
        return document.copy(
            metadata = document.metadata.copy(
                key = newKey,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun createEmptySection(type: SongSectionType, order: Int): SongSection {
        return SongSection(
            id = UUID.randomUUID().toString(),
            title = type.name,
            type = type,
            lyrics = "",
            chords = "",
            order = order
        )
    }

    fun removeSection(document: SongDocument, sectionId: String): SongDocument {
        val newSections = document.sections.filter { it.id != sectionId }
            .sortedBy { it.order }
            .mapIndexed { index, section -> section.copy(order = index) }
        
        return document.copy(
            sections = newSections,
            metadata = document.metadata.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
