package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ChordTransposerTest {

    @Test
    fun testTransposeBasicNotes() {
        assertEquals("D", ChordTransposer.transposeNote("C", 2, useFlats = false))
        assertEquals("B", ChordTransposer.transposeNote("C", -1, useFlats = false))
        assertEquals("F#", ChordTransposer.transposeNote("E", 2, useFlats = false))
        assertEquals("Gb", ChordTransposer.transposeNote("E", 2, useFlats = true))
        
        // Flats parsing tests
        assertEquals("A", ChordTransposer.transposeNote("Ab", 1, useFlats = false))
        assertEquals("G", ChordTransposer.transposeNote("Ab", -1, useFlats = false))
    }

    @Test
    fun testTransposeBasicChords() {
        assertEquals("Em", ChordTransposer.transposeChord("Dm", 2, useFlats = false))
        assertEquals("F#m", ChordTransposer.transposeChord("Em", 2, useFlats = false))
        assertEquals("Gbm", ChordTransposer.transposeChord("Em", 2, useFlats = true))
        
        assertEquals("D7", ChordTransposer.transposeChord("C7", 2, useFlats = false))
        assertEquals("Amaj7", ChordTransposer.transposeChord("Gmaj7", 2, useFlats = false))
    }

    @Test
    fun testTransposeChordsWithBass() {
        assertEquals("D/F#", ChordTransposer.transposeChord("C/E", 2, useFlats = false))
        assertEquals("Eb/G", ChordTransposer.transposeChord("D/F#", 1, useFlats = true))
        assertEquals("Bb/D", ChordTransposer.transposeChord("C/E", -2, useFlats = true))
    }

    @Test
    fun testTransposeComplexChords() {
        // Suspended, diminished, augmented
        assertEquals("F#sus4", ChordTransposer.transposeChord("Esus4", 2, useFlats = false))
        assertEquals("C°", ChordTransposer.transposeChord("B°", 1, useFlats = false))
        assertEquals("Baug", ChordTransposer.transposeChord("Aaug", 2, useFlats = false))
        assertEquals("Abm11", ChordTransposer.transposeChord("Gm11", 1, useFlats = true))
    }

    @Test
    fun testIsChord() {
        assertEquals(true, ChordTransposer.isChord("D"))
        assertEquals(true, ChordTransposer.isChord("F#m7"))
        assertEquals(true, ChordTransposer.isChord("C/E"))
        assertEquals(true, ChordTransposer.isChord("Bbmaj7/D"))
        assertEquals(true, ChordTransposer.isChord("C°"))
        
        // Negative cases
        assertEquals(false, ChordTransposer.isChord("Hello"))
        assertEquals(false, ChordTransposer.isChord("Dog"))
        assertEquals(false, ChordTransposer.isChord("A/Hello"))
    }

    @Test
    fun testTransposeText() {
        val inputText = "D A Bm G"
        // transpose +1 with flats
        val outputTextFlat = ChordTransposer.transposeText(inputText, 1, useFlats = true)
        assertEquals("Eb Bb Cm Ab", outputTextFlat)

        // transpose +2 with sharps
        val outputTextSharp = ChordTransposer.transposeText(inputText, 2, useFlats = false)
        assertEquals("E B C#m A", outputTextSharp)
        
        val mixedText = "Intro: D/F# G A Bm7"
        val expectedMixedText = "Intro: E/G# A B C#m7"
        assertEquals(expectedMixedText, ChordTransposer.transposeText(mixedText, 2, useFlats = false))
    }
}
