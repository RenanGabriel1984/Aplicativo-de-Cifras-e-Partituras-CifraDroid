package com.example.domain.harmony

data class TranspositionResult(
    val original: MusicalChord,
    val transposed: MusicalChord,
    val steps: Int
)
