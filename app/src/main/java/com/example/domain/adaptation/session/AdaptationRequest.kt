package com.example.domain.adaptation.session

import com.example.domain.adaptation.AdaptationType

data class AdaptationRequest(
    val type: AdaptationType,
    val targetKey: String?,
    val targetCapo: Int?,
    val profileName: String,
    val profileDescription: String
)
