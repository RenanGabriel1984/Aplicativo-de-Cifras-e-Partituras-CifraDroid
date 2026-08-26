package com.example.domain.harmony

data class MusicalPitch(val note: MusicalNote, val accidental: Accidental = Accidental.NATURAL) {
    val chromaticValue: Int
        get() = (note.baseChromaticValue + accidental.semitoneOffset + 12) % 12
        
    override fun toString(): String = "${note.name}${accidental.symbol}"
}
