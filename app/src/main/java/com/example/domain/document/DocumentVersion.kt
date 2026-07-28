package com.example.domain.document

data class DocumentVersion(
    val version: String,
    val createdAt: Long,
    val description: String
)
