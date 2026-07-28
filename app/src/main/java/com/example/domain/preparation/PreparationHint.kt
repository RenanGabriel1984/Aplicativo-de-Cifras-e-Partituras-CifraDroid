package com.example.domain.preparation

data class PreparationHint(
    val id: String,
    val title: String,
    val description: String,
    val severity: PreparationSeverity
)
