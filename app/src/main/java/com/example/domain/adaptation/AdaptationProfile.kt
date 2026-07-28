package com.example.domain.adaptation

data class AdaptationProfile(
    val id: String,
    val name: String,
    val description: String,
    val rules: List<AdaptationRule>
)
