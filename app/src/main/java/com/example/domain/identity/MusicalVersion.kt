package com.example.domain.identity

data class MusicalVersion(
    val id: String,
    val origin: MusicalOrigin,
    val createdAt: Long,
    val documentId: String,
    val description: String
)
