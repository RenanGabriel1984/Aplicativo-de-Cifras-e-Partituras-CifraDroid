package com.example.domain.identity

data class MusicalOrigin(
    val source: MusicalSource,
    val sourceId: String? = null,
    val url: String? = null
)
