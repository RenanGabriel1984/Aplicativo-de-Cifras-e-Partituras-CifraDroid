package com.example.domain.harmony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class HarmonyCoreTest {
    
    @Test
    fun `test chord parsing and formatting`() {
        val cases = listOf(
            "C" to "C",
            "Cm" to "Cm",
            "C7" to "C7",
            "Cmaj7" to "Cmaj7",
            "C7M" to "Cmaj7",
            "Cm7" to "Cm7",
            "Cm7(b5)" to "Cm7(b5)",
            "Cadd9" to "Cadd9",
            "C9" to "C9",
            "C11" to "C11",
            "C13" to "C13",
            "Csus2" to "Csus2",
            "Csus4" to "Csus4",
            "Cdim" to "Cdim",
            "Caug" to "Caug",
            "C/E" to "C/E",
            "C/G" to "C/G",
            "F#m7(b5)/A" to "F#m7(b5)/A",
            "Bbmaj7" to "Bbmaj7",
            "A#7" to "A#7"
        )
        
        for ((input, expected) in cases) {
            val parsed = HarmonyEngine.parse(input)
            assertNotNull("Failed to parse \$input", parsed)
            val formatted = HarmonyFormatter.format(parsed!!, HarmonicDisplayProfile.AUTOMATIC)
            assertEquals("Parsing/Formatting mismatch for \$input", expected, formatted)
        }
    }
    
    @Test
    fun `test transposition`() {
        // C -> D (+2)
        val c = HarmonyEngine.parse("C")!!
        val transC = HarmonicTransformationEngine.transpose(c, 2, HarmonicDisplayProfile.SHARP)
        assertEquals("D", HarmonyEngine.format(transC, HarmonicDisplayProfile.SHARP))
        
        // G -> A (+2)
        val g = HarmonyEngine.parse("G")!!
        val transG = HarmonicTransformationEngine.transpose(g, 2, HarmonicDisplayProfile.SHARP)
        assertEquals("A", HarmonyEngine.format(transG, HarmonicDisplayProfile.SHARP))
        
        // Bb -> B (+1)
        val bb = HarmonyEngine.parse("Bb")!!
        val transBb = HarmonicTransformationEngine.transpose(bb, 1, HarmonicDisplayProfile.SHARP)
        assertEquals("B", HarmonyEngine.format(transBb, HarmonicDisplayProfile.SHARP))
        
        // A# -> C (+2)
        val aSharp = HarmonyEngine.parse("A#")!!
        val transASharp = HarmonicTransformationEngine.transpose(aSharp, 2, HarmonicDisplayProfile.SHARP)
        assertEquals("C", HarmonyEngine.format(transASharp, HarmonicDisplayProfile.SHARP))
        
        // F#m7(b5)/A -> G#m7(b5)/B (+2)
        val complex = HarmonyEngine.parse("F#m7(b5)/A")!!
        val transComplex = HarmonicTransformationEngine.transpose(complex, 2, HarmonicDisplayProfile.SHARP)
        assertEquals("G#m7(b5)/B", HarmonyEngine.format(transComplex, HarmonicDisplayProfile.SHARP))
    }
    
    @Test
    fun `test profiles`() {
        val aSharp = HarmonyEngine.parse("A#")!!
        assertEquals("A#", HarmonyEngine.format(aSharp, HarmonicDisplayProfile.SHARP))
        assertEquals("Bb", HarmonyEngine.format(aSharp, HarmonicDisplayProfile.FLAT))
        
        val bb = HarmonyEngine.parse("Bb")!!
        assertEquals("A#", HarmonyEngine.format(bb, HarmonicDisplayProfile.SHARP))
        assertEquals("Bb", HarmonyEngine.format(bb, HarmonicDisplayProfile.FLAT))
    }
}
