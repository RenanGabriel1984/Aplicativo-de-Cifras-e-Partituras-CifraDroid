package com.example.domain.harmony

object HarmonicTransformationEngine {
    fun transpose(chord: MusicalChord, steps: Int, profile: HarmonicDisplayProfile = HarmonicDisplayProfile.AUTOMATIC): MusicalChord {
        val newRoot = transposePitch(chord.root, steps)
        val newBass = chord.bass?.let { transposePitch(it, steps) }
        
        val formatProfile = if (profile == HarmonicDisplayProfile.AUTOMATIC) {
            val c = newRoot.chromaticValue
            val preferSharp = c !in listOf(1, 3, 5, 8, 10) // default sharp unless Db, Eb, F, Ab, Bb
            if (preferSharp) HarmonicDisplayProfile.SHARP else HarmonicDisplayProfile.FLAT
        } else {
            profile
        }
        
        val eqRoot = EnharmonicEquivalent.getEquivalent(newRoot, formatProfile)
        val eqBass = newBass?.let { EnharmonicEquivalent.getEquivalent(it, formatProfile) }
        
        return chord.copy(root = eqRoot, bass = eqBass)
    }
    
    private fun transposePitch(pitch: MusicalPitch, steps: Int): MusicalPitch {
        val chromatic = (pitch.chromaticValue + steps) % 12
        val normalized = if (chromatic < 0) chromatic + 12 else chromatic
        return EnharmonicEquivalent.getPitchByChromatic(normalized, preferSharp = true)
    }
}
