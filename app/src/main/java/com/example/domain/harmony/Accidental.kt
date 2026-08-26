package com.example.domain.harmony

enum class Accidental(val symbol: String, val semitoneOffset: Int) {
    NATURAL("", 0),
    SHARP("#", 1),
    FLAT("b", -1)
}
