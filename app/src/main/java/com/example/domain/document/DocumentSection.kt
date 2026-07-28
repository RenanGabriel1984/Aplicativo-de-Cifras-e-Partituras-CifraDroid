package com.example.domain.document

import com.example.util.PdfAnnotation

data class DocumentSection(
    val id: String,
    val title: String,
    val semanticType: String,
    val lyrics: String,
    val chords: String,
    val annotations: List<PdfAnnotation>,
    val order: Int
)
