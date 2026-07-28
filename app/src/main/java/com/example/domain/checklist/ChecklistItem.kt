package com.example.domain.checklist

data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ChecklistCategory,
    val status: ChecklistStatus,
    val relatedSongId: String? = null
)
