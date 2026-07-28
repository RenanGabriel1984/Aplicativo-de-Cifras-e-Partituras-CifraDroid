package com.example.domain.models

data class SongMetadata(
    val id: String,
    val title: String,
    val artist: String,
    val composer: String,
    val key: String,
    val capo: Int,
    val bpm: Int,
    val timeSignature: String,
    val category: SongCategory,
    val tags: List<SongTag>,
    val createdAt: Long,
    val updatedAt: Long
)
