package com.example.domain.harmony

data class MusicalChord(
    val root: MusicalPitch,
    val quality: ChordQuality = ChordQuality.MAJOR,
    val extension: ChordExtension = ChordExtension.NONE,
    val alterations: List<ChordAlteration> = emptyList(),
    val bass: MusicalPitch? = null
)
