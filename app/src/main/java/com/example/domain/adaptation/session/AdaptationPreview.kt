package com.example.domain.adaptation.session

data class AdaptationPreview(
    val originalKey: String,
    val originalCapo: Int,
    val targetKey: String,
    val targetCapo: Int,
    val warnings: List<String>
)
