package com.example.domain.editor

import com.example.domain.document.MusicalDocument
import com.example.domain.document.DocumentSection
import java.util.UUID

object MusicalStructureEditor {

    fun insertSection(document: MusicalDocument, section: DocumentSection, index: Int): MusicalDocument {
        val newSections = document.sections.toMutableList()
        val safeIndex = index.coerceIn(0, newSections.size)
        newSections.add(safeIndex, section)
        return document.copy(sections = updateOrder(newSections))
    }

    fun removeSection(document: MusicalDocument, sectionId: String): MusicalDocument {
        val newSections = document.sections.filter { it.id != sectionId }
        return document.copy(sections = updateOrder(newSections))
    }

    fun moveSection(document: MusicalDocument, sectionId: String, newIndex: Int): MusicalDocument {
        val section = document.sections.find { it.id == sectionId } ?: return document
        val newSections = document.sections.toMutableList()
        newSections.remove(section)
        
        val safeIndex = newIndex.coerceIn(0, newSections.size)
        newSections.add(safeIndex, section)
        return document.copy(sections = updateOrder(newSections))
    }

    fun duplicateSection(document: MusicalDocument, sectionId: String): MusicalDocument {
        val sectionIndex = document.sections.indexOfFirst { it.id == sectionId }
        if (sectionIndex == -1) return document
        
        val sectionToDuplicate = document.sections[sectionIndex]
        val newSection = sectionToDuplicate.copy(
            id = UUID.randomUUID().toString(),
            title = sectionToDuplicate.title + " (Copy)"
        )
        
        val newSections = document.sections.toMutableList()
        newSections.add(sectionIndex + 1, newSection)
        return document.copy(sections = updateOrder(newSections))
    }

    fun mergeSections(document: MusicalDocument, sectionId1: String, sectionId2: String): MusicalDocument {
        val s1 = document.sections.find { it.id == sectionId1 } ?: return document
        val s2 = document.sections.find { it.id == sectionId2 } ?: return document
        
        val newSection = s1.copy(
            lyrics = s1.lyrics + "\n" + s2.lyrics,
            chords = s1.chords + "\n" + s2.chords,
            annotations = s1.annotations + s2.annotations
        )
        
        val newSections = document.sections.map { 
            if (it.id == sectionId1) newSection else it 
        }.filter { it.id != sectionId2 }
        
        return document.copy(sections = updateOrder(newSections))
    }

    fun splitSection(document: MusicalDocument, sectionId: String, splitIndexLyrics: Int, splitIndexChords: Int): MusicalDocument {
        val sectionIndex = document.sections.indexOfFirst { it.id == sectionId }
        if (sectionIndex == -1) return document
        
        val s = document.sections[sectionIndex]
        val safeLyricsIndex = splitIndexLyrics.coerceIn(0, s.lyrics.length)
        val safeChordsIndex = splitIndexChords.coerceIn(0, s.chords.length)
        
        val s1 = s.copy(
            id = UUID.randomUUID().toString(),
            title = s.title + " (Part 1)",
            lyrics = s.lyrics.substring(0, safeLyricsIndex),
            chords = s.chords.substring(0, safeChordsIndex)
        )
        
        val s2 = s.copy(
            id = UUID.randomUUID().toString(),
            title = s.title + " (Part 2)",
            lyrics = s.lyrics.substring(safeLyricsIndex),
            chords = s.chords.substring(safeChordsIndex)
        )
        
        val newSections = document.sections.toMutableList()
        newSections.removeAt(sectionIndex)
        newSections.add(sectionIndex, s2)
        newSections.add(sectionIndex, s1)
        
        return document.copy(sections = updateOrder(newSections))
    }

    fun renameSection(document: MusicalDocument, sectionId: String, newTitle: String): MusicalDocument {
        val newSections = document.sections.map { 
            if (it.id == sectionId) it.copy(title = newTitle) else it 
        }
        return document.copy(sections = newSections)
    }

    fun changeSectionType(document: MusicalDocument, sectionId: String, newSemanticType: String): MusicalDocument {
        val newSections = document.sections.map { 
            if (it.id == sectionId) it.copy(semanticType = newSemanticType) else it 
        }
        return document.copy(sections = newSections)
    }
    
    private fun updateOrder(sections: List<DocumentSection>): List<DocumentSection> {
        return sections.mapIndexed { index, section ->
            section.copy(order = index)
        }
    }
}
