package com.example.domain.document

import com.example.domain.models.SongMetadata

data class MusicalDocument(
    val id: String,
    val metadata: SongMetadata,
    val structure: DocumentStructure,
    val sections: List<DocumentSection>,
    val statistics: DocumentStatistics,
    val versions: List<DocumentVersion>,
    val createdAt: Long,
    val updatedAt: Long
)
