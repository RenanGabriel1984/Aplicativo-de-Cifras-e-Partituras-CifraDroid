package com.example.domain.harmony

object HarmonyEngine {
    fun parse(chordStr: String): MusicalChord? {
        return HarmonyParser.parse(chordStr)
    }
    
    fun format(chord: MusicalChord, profile: HarmonicDisplayProfile = HarmonicDisplayProfile.AUTOMATIC): String {
        return HarmonyFormatter.format(chord, profile)
    }
}
