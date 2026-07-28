package com.example.domain.adaptation

data class AdaptationRule(
    val id: String,
    val type: AdaptationType,
    val parameters: Map<String, String>,
    val description: String
)
