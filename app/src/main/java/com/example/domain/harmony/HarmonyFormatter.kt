package com.example.domain.harmony

object HarmonyFormatter {
    fun format(chord: MusicalChord, profile: HarmonicDisplayProfile = HarmonicDisplayProfile.AUTOMATIC): String {
        val root = EnharmonicEquivalent.getEquivalent(chord.root, profile)
        val sb = java.lang.StringBuilder()
        sb.append(root.note.name).append(root.accidental.symbol)
        
        when (chord.quality) {
            ChordQuality.MINOR -> sb.append("m")
            ChordQuality.DIMINISHED -> sb.append("dim")
            ChordQuality.AUGMENTED -> sb.append("aug")
            ChordQuality.SUS2 -> sb.append("sus2")
            ChordQuality.SUS4 -> sb.append("sus4")
            ChordQuality.MAJOR -> {}
        }
        
        when (chord.extension) {
            ChordExtension.MAJOR_SEVENTH -> sb.append("maj7")
            ChordExtension.SEVENTH -> sb.append("7")
            ChordExtension.ADD9 -> sb.append("add9")
            ChordExtension.NINTH -> sb.append("9")
            ChordExtension.ELEVENTH -> sb.append("11")
            ChordExtension.THIRTEENTH -> sb.append("13")
            ChordExtension.SIXTH -> sb.append("6")
            ChordExtension.NONE -> {}
        }
        
        for (alt in chord.alterations) {
            sb.append("(").append(alt.accidental.symbol).append(alt.degree).append(")")
        }
        
        if (chord.bass != null) {
            val bass = EnharmonicEquivalent.getEquivalent(chord.bass, profile)
            sb.append("/").append(bass.note.name).append(bass.accidental.symbol)
        }
        
        return sb.toString()
    }
}
