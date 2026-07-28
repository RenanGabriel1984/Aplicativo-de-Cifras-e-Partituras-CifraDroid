package com.example.domain.library

enum class FilterType {
    CATEGORY,
    KEY,
    BPM,
    CAPO,
    LITURGICAL_SEASON,
    ARTIST,
    LANGUAGE,
    LAST_ACCESSED,
    MOST_PLAYED
}

data class LibraryFilter(
    val id: String,
    val name: String,
    val type: FilterType,
    val value: String
)
