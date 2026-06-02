package com.example.util

object ChordTransposer {

    private val sharps = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val flats = arrayOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    // Covers common chord suffixes and optional bass notes
    private val chordRegex = Regex("^[A-G][#b]?(?:m|M|maj|min|dim|aug|sus|\\d|º|°|\\+|-)*(?:/[A-G][#b]?)?$")

    /**
     * Finds the chromatic index of a given note (0 to 11).
     * @return the index, or -1 if the note is invalid.
     */
    fun getNoteIndex(note: String): Int {
        if (note.isEmpty()) return -1
        val upperNote = note.replaceFirstChar { it.uppercase() }
        val indexSharp = sharps.indexOf(upperNote)
        if (indexSharp != -1) return indexSharp
        val indexFlat = flats.indexOf(upperNote)
        return if (indexFlat != -1) indexFlat else -1
    }
    
    /**
     * Calculates the number of half-steps from one note to another.
     */
    fun getStepsBetween(fromNote: String, toNote: String): Int {
        val regex = Regex("^([A-G][#b]?).*")
        val fromMatch = regex.matchEntire(fromNote.trim())
        val toMatch = regex.matchEntire(toNote.trim())
        
        if (fromMatch == null || toMatch == null) return 0
        
        val fromRoot = fromMatch.groupValues[1]
        val toRoot = toMatch.groupValues[1]
        
        val fromIndex = getNoteIndex(fromRoot)
        val toIndex = getNoteIndex(toRoot)
        
        if (fromIndex == -1 || toIndex == -1) return 0
        
        var steps = toIndex - fromIndex
        if (steps < -6) steps += 12
        if (steps > 6) steps -= 12
        return steps
    }

    /**
     * Transposes a single note by the given half-steps.
     */
    fun transposeNote(note: String, steps: Int, useFlats: Boolean): String {
        val index = getNoteIndex(note)
        if (index == -1) return note
        
        val newIndex = (index + steps) % 12
        val normalizedIndex = if (newIndex < 0) newIndex + 12 else newIndex
        
        return if (useFlats) flats[normalizedIndex] else sharps[normalizedIndex]
    }

    /**
     * Transposes a single chord string.
     * Example: "D/F#" + 1 step -> "Eb/G" (if useFlats = true)
     */
    fun transposeChord(chord: String, steps: Int, useFlats: Boolean = false): String {
        val regex = Regex("^([A-G][#b]?)(.*?)(?:/([A-G][#b]?))?$")
        val match = regex.matchEntire(chord) ?: return chord
        
        val rootNote = match.groupValues[1]
        val suffix = match.groupValues[2]
        val bassNote = match.groupValues[3]
        
        val transposedRoot = transposeNote(rootNote, steps, useFlats)
        
        return buildString {
            append(transposedRoot)
            append(suffix)
            if (bassNote.isNotEmpty()) {
                append("/")
                append(transposeNote(bassNote, steps, useFlats))
            }
        }
    }

    /**
     * Checks if a string looks entirely like a valid chord.
     */
    fun isChord(token: String): Boolean {
        return chordRegex.matches(token.trim())
    }

    /**
     * Transposes a full text containing chords, preserving whitespaces.
     * Replaces words that match the chord pattern.
     */
    fun transposeText(text: String, steps: Int, useFlats: Boolean = false): String {
        if (steps == 0) return text
        val regex = Regex("(\\s+)|([^\\s]+)")
        return buildString {
            for (match in regex.findAll(text)) {
                val token = match.value
                if (token.trim().isNotEmpty() && isChord(token)) {
                    append(transposeChord(token, steps, useFlats))
                } else {
                    append(token)
                }
            }
        }
    }

    /**
     * Extracts unique chords from a given text, preserving insertion order.
     */
    fun extractUniqueChords(text: String): List<String> {
        val regex = Regex("[^\\s]+")
        val chords = mutableListOf<String>()
        for (match in regex.findAll(text)) {
            val token = match.value.trim()
            if (token.isNotEmpty() && isChord(token) && !chords.contains(token)) {
                chords.add(token)
            }
        }
        return chords
    }
}
