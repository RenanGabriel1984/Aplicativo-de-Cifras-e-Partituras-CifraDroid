package com.example.util

data class StructureSection(
    val id: String,
    val name: String,
    val type: StructureType,
    val startPage: Int,
    val endPage: Int,
    val markers: List<ScoreMarker>
)
