package com.example.domain.analysis

import com.example.domain.document.MusicalDocument

object MusicalAnalysisEngine {

    fun analyze(document: MusicalDocument): MusicalAnalysis {
        return MusicalAnalysis(
            statistics = countSections(document),
            sequence = generateStructure(document),
            estimatedDurationSeconds = calculateEstimatedDuration(document),
            complexity = calculateComplexity(document),
            alerts = detectMissingSections(document)
        )
    }

    fun countSections(document: MusicalDocument): MusicalAnalysisStatistics {
        var verseCount = 0
        var chorusCount = 0
        var bridgeCount = 0
        var soloCount = 0
        
        document.sections.forEach { section ->
            when (section.semanticType.uppercase()) {
                "VERSE" -> verseCount++
                "CHORUS" -> chorusCount++
                "BRIDGE" -> bridgeCount++
                "SOLO" -> soloCount++
            }
        }
        
        return MusicalAnalysisStatistics(
            verseCount = verseCount,
            chorusCount = chorusCount,
            bridgeCount = bridgeCount,
            soloCount = soloCount,
            totalSections = document.sections.size
        )
    }

    fun countRepeats(document: MusicalDocument): Int {
        var repeats = 0
        document.sections.forEach { section ->
            if (section.title.contains("x2", ignoreCase = true)) {
                repeats++
            } else {
                for (ann in section.annotations) {
                    if (ann.text != null && ann.text.contains("repeat", ignoreCase = true)) {
                        repeats++
                        break
                    }
                }
            }
        }
        return repeats
    }

    fun calculateEstimatedDuration(document: MusicalDocument): Int {
        val baseSecondsPerSection = 30
        return document.sections.size * baseSecondsPerSection
    }

    fun calculateComplexity(document: MusicalDocument): String {
        val uniqueChords = document.sections.flatMap { it.chords.split(Regex("\\s+")) }.filter { it.isNotBlank() }.toSet().size
        return when {
            uniqueChords > 15 -> "HIGH"
            uniqueChords > 7 -> "MEDIUM"
            else -> "LOW"
        }
    }

    fun detectMissingSections(document: MusicalDocument): List<String> {
        val alerts = mutableListOf<String>()
        val types = document.sections.map { it.semanticType.uppercase() }
        
        if (!types.contains("CHORUS")) {
            alerts.add("Música não possui refrão.")
        }
        
        if (!types.contains("VERSE")) {
            alerts.add("Música não possui versos.")
        }
        
        return alerts
    }

    fun generateStructure(document: MusicalDocument): List<String> {
        return document.sections.sortedBy { it.order }.map { it.semanticType.uppercase() }
    }
}
