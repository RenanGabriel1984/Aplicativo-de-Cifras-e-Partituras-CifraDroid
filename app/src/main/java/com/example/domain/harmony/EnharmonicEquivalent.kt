package com.example.domain.harmony

object EnharmonicEquivalent {
    fun getEquivalent(pitch: MusicalPitch, profile: HarmonicDisplayProfile): MusicalPitch {
        if (profile == HarmonicDisplayProfile.AUTOMATIC) return pitch
        if (pitch.accidental == Accidental.NATURAL) return pitch
        
        return when (profile) {
            HarmonicDisplayProfile.SHARP -> toSharp(pitch)
            HarmonicDisplayProfile.FLAT -> toFlat(pitch)
            else -> pitch
        }
    }
    
    private fun toSharp(pitch: MusicalPitch): MusicalPitch {
        if (pitch.accidental == Accidental.SHARP || pitch.accidental == Accidental.NATURAL) return pitch
        return getPitchByChromatic(pitch.chromaticValue, true)
    }
    
    private fun toFlat(pitch: MusicalPitch): MusicalPitch {
        if (pitch.accidental == Accidental.FLAT || pitch.accidental == Accidental.NATURAL) return pitch
        return getPitchByChromatic(pitch.chromaticValue, false)
    }
    
    fun getPitchByChromatic(chromaticValue: Int, preferSharp: Boolean): MusicalPitch {
        val normalized = (chromaticValue % 12 + 12) % 12
        return when (normalized) {
            0 -> MusicalPitch(MusicalNote.C, Accidental.NATURAL)
            1 -> if (preferSharp) MusicalPitch(MusicalNote.C, Accidental.SHARP) else MusicalPitch(MusicalNote.D, Accidental.FLAT)
            2 -> MusicalPitch(MusicalNote.D, Accidental.NATURAL)
            3 -> if (preferSharp) MusicalPitch(MusicalNote.D, Accidental.SHARP) else MusicalPitch(MusicalNote.E, Accidental.FLAT)
            4 -> MusicalPitch(MusicalNote.E, Accidental.NATURAL)
            5 -> MusicalPitch(MusicalNote.F, Accidental.NATURAL)
            6 -> if (preferSharp) MusicalPitch(MusicalNote.F, Accidental.SHARP) else MusicalPitch(MusicalNote.G, Accidental.FLAT)
            7 -> MusicalPitch(MusicalNote.G, Accidental.NATURAL)
            8 -> if (preferSharp) MusicalPitch(MusicalNote.G, Accidental.SHARP) else MusicalPitch(MusicalNote.A, Accidental.FLAT)
            9 -> MusicalPitch(MusicalNote.A, Accidental.NATURAL)
            10 -> if (preferSharp) MusicalPitch(MusicalNote.A, Accidental.SHARP) else MusicalPitch(MusicalNote.B, Accidental.FLAT)
            11 -> MusicalPitch(MusicalNote.B, Accidental.NATURAL)
            else -> throw IllegalArgumentException("Invalid chromatic value")
        }
    }
}
