package com.example.domain.models

import com.example.util.PdfAnnotation

data class SongDocument(
    val metadata: SongMetadata,
    val sections: List<SongSection>,
    val annotations: List<PdfAnnotation>,
    val linkedPdf: String?,
    val linkedAudio: String?
)
