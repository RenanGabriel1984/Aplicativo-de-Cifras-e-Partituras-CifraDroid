package com.example.util

data class ConductorSection(
    val id: String,
    val type: MusicalSemanticType,
    val startPage: Int,
    val endPage: Int,
    val active: Boolean,
    val completed: Boolean
)
