package com.example.domain.document

import com.example.util.MusicalSemanticBlock
import com.example.util.MusicalTimeline

data class DocumentStructure(
    val sections: List<DocumentSection>,
    val relationships: List<String>,
    val timeline: MusicalTimeline,
    val semanticBlocks: List<MusicalSemanticBlock>
)
