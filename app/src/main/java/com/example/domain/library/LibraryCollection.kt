package com.example.domain.library

import com.example.domain.models.SongDocument

data class LibraryCollection(
    val id: String,
    val title: String,
    val icon: String,
    val color: String,
    val documents: List<SongDocument>
)
