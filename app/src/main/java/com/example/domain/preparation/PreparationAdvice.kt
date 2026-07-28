package com.example.domain.preparation

data class PreparationAdvice(
    val title: String,
    val message: String,
    val priority: Int,
    val actionSuggested: PreparationAction? = null
)
