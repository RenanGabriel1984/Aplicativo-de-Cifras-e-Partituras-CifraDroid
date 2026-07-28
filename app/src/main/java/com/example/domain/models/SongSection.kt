package com.example.domain.models

enum class SongSectionType {
    INTRO,
    VERSE,
    PRE_CHORUS,
    CHORUS,
    BRIDGE,
    SOLO,
    INSTRUMENTAL,
    CODA,
    OUTRO,
    ENDING,
    CUSTOM
}

data class SongSection(
    val id: String,
    val title: String,
    val type: SongSectionType,
    val lyrics: String,
    val chords: String,
    val order: Int
)
