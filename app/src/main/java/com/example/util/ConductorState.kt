package com.example.util

data class ConductorState(
    val sections: List<ConductorSection>,
    val currentSection: Int,
    val progress: Float
)
