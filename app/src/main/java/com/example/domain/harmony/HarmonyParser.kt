package com.example.domain.harmony

object HarmonyParser {
    fun parse(chordStr: String): MusicalChord? {
        val match = HarmonyConstants.CHORD_REGEX.matchEntire(chordStr.trim()) ?: return null
        
        val noteStr = match.groupValues[1]
        val accStr = match.groupValues[2]
        val rest = match.groupValues[3]
        
        val note = MusicalNote.valueOf(noteStr)
        val accidental = when (accStr) {
            "#" -> Accidental.SHARP
            "b" -> Accidental.FLAT
            else -> Accidental.NATURAL
        }
        val root = MusicalPitch(note, accidental)
        
        val parts = rest.split("/")
        var modifiers = parts[0]
        val bassStr = if (parts.size > 1) parts[1] else null
        
        var bass: MusicalPitch? = null
        if (bassStr != null) {
            val bMatch = Regex("^([A-G])([#b]?)$").matchEntire(bassStr)
            if (bMatch != null) {
                val bNote = MusicalNote.valueOf(bMatch.groupValues[1])
                val bAcc = when (bMatch.groupValues[2]) {
                    "#" -> Accidental.SHARP
                    "b" -> Accidental.FLAT
                    else -> Accidental.NATURAL
                }
                bass = MusicalPitch(bNote, bAcc)
            }
        }
        
        var quality = ChordQuality.MAJOR
        
        if (modifiers.startsWith("m") && !modifiers.startsWith("maj") && !modifiers.startsWith("min") && !modifiers.startsWith("m7(b5)")) {
            quality = ChordQuality.MINOR
            modifiers = modifiers.substring(1)
        } else if (modifiers.startsWith("min")) {
            quality = ChordQuality.MINOR
            modifiers = modifiers.substring(3)
        } else if (modifiers.startsWith("dim") || modifiers.startsWith("°")) {
            quality = ChordQuality.DIMINISHED
            modifiers = modifiers.replaceFirst(Regex("dim|°"), "")
        } else if (modifiers.startsWith("aug") || modifiers.startsWith("+")) {
            quality = ChordQuality.AUGMENTED
            modifiers = modifiers.replaceFirst(Regex("aug|\\+"), "")
        } else if (modifiers.startsWith("sus2")) {
            quality = ChordQuality.SUS2
            modifiers = modifiers.substring(4)
        } else if (modifiers.startsWith("sus4")) {
            quality = ChordQuality.SUS4
            modifiers = modifiers.substring(4)
        } else if (modifiers.startsWith("sus")) {
            quality = ChordQuality.SUS4
            modifiers = modifiers.substring(3)
        } else if (modifiers.startsWith("m7(b5)")) {
            quality = ChordQuality.MINOR
            modifiers = "7(b5)" + modifiers.substring(6)
        }
        
        var extension = ChordExtension.NONE
        
        if (modifiers.startsWith("maj7") || modifiers.startsWith("M7") || modifiers.startsWith("7M")) {
            extension = ChordExtension.MAJOR_SEVENTH
            modifiers = modifiers.replaceFirst(Regex("maj7|M7|7M"), "")
        } else if (modifiers.startsWith("7")) {
            extension = ChordExtension.SEVENTH
            modifiers = modifiers.substring(1)
        } else if (modifiers.startsWith("add9")) {
            extension = ChordExtension.ADD9
            modifiers = modifiers.substring(4)
        } else if (modifiers.startsWith("9")) {
            extension = ChordExtension.NINTH
            modifiers = modifiers.substring(1)
        } else if (modifiers.startsWith("11")) {
            extension = ChordExtension.ELEVENTH
            modifiers = modifiers.substring(2)
        } else if (modifiers.startsWith("13")) {
            extension = ChordExtension.THIRTEENTH
            modifiers = modifiers.substring(2)
        } else if (modifiers.startsWith("6")) {
            extension = ChordExtension.SIXTH
            modifiers = modifiers.substring(1)
        }
        
        val alterations = mutableListOf<ChordAlteration>()
        val altRegex = Regex("\\(?(b|#)(\\d+)\\)?")
        val altMatches = altRegex.findAll(modifiers)
        for (m in altMatches) {
            val acc = if (m.groupValues[1] == "#") Accidental.SHARP else Accidental.FLAT
            val deg = m.groupValues[2].toInt()
            alterations.add(ChordAlteration(acc, deg))
        }
        
        return MusicalChord(root, quality, extension, alterations, bass)
    }
}
