package com.example.domain.harmony

data class ChordAlteration(val accidental: Accidental, val degree: Int) {
    override fun toString(): String = "${accidental.symbol}$degree"
}
