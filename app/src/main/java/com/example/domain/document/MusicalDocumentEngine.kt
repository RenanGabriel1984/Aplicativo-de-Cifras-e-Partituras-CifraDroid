package com.example.domain.document

import com.example.domain.models.SongMetadata
import com.example.util.MusicalTimeline
import java.util.UUID

object MusicalDocumentEngine {

    fun createDocument(metadata: SongMetadata): MusicalDocument {
        val now = System.currentTimeMillis()
        return MusicalDocument(
            id = UUID.randomUUID().toString(),
            metadata = metadata,
            structure = DocumentStructure(
                sections = emptyList(),
                relationships = emptyList(),
                timeline = MusicalTimeline(),
                semanticBlocks = emptyList()
            ),
            sections = emptyList(),
            statistics = DocumentStatistics(0, 0, 0, 0L, "UNKNOWN"),
            versions = listOf(DocumentVersion("1.0", now, "Initial Version")),
            createdAt = now,
            updatedAt = now
        )
    }

    fun duplicateDocument(document: MusicalDocument): MusicalDocument {
        val now = System.currentTimeMillis()
        val newId = UUID.randomUUID().toString()
        return document.copy(
            id = newId,
            metadata = document.metadata.copy(id = newId, title = "${document.metadata.title} (Copy)", updatedAt = now),
            versions = listOf(DocumentVersion("1.0", now, "Duplicated Version")),
            createdAt = now,
            updatedAt = now
        )
    }

    fun calculateStatistics(document: MusicalDocument): MusicalDocument {
        var lyricsLines = 0
        var chordLines = 0
        document.sections.forEach { section ->
            lyricsLines += section.lyrics.lines().filter { it.isNotBlank() }.size
            chordLines += section.chords.lines().filter { it.isNotBlank() }.size
        }
        val estimatedDuration = document.sections.size * 30000L
        
        return document.copy(
            statistics = document.statistics.copy(
                totalSections = document.sections.size,
                totalLyricsLines = lyricsLines,
                totalChordLines = chordLines,
                estimatedDuration = estimatedDuration
            ),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun createVersion(document: MusicalDocument, description: String): MusicalDocument {
        val newVersionNumber = "${document.versions.size + 1}.0"
        val newVersion = DocumentVersion(newVersionNumber, System.currentTimeMillis(), description)
        return document.copy(
            versions = document.versions + newVersion,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun rebuildStructure(document: MusicalDocument): MusicalDocument {
        val sortedSections = document.sections.sortedBy { it.order }
        return document.copy(
            structure = document.structure.copy(sections = sortedSections),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateSection(document: MusicalDocument, sectionId: String, updatedSection: DocumentSection): MusicalDocument {
        val newSections = document.sections.map { 
            if (it.id == sectionId) updatedSection else it
        }
        return rebuildStructure(document.copy(sections = newSections, updatedAt = System.currentTimeMillis()))
    }

    fun addSection(document: MusicalDocument, section: DocumentSection): MusicalDocument {
        val newSections = document.sections + section
        return rebuildStructure(document.copy(sections = newSections, updatedAt = System.currentTimeMillis()))
    }

    fun removeSection(document: MusicalDocument, sectionId: String): MusicalDocument {
        val newSections = document.sections.filter { it.id != sectionId }
            .sortedBy { it.order }
            .mapIndexed { index, section -> section.copy(order = index) }
        return rebuildStructure(document.copy(sections = newSections, updatedAt = System.currentTimeMillis()))
    }
}
