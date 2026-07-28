package com.example.domain.identity

data class MusicalIdentity(
    val id: String,
    val title: String,
    val artist: String,
    val category: String,
    val originalKey: String,
    val bpm: Int,
    val language: String,
    val liturgicalSeason: String? = null,
    val ministry: String? = null,
    val versions: List<MusicalVersion>,
    val primaryVersion: MusicalVersion? = null
)
