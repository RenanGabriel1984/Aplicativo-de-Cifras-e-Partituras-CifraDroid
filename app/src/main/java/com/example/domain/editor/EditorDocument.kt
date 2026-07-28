package com.example.domain.editor

import com.example.domain.document.MusicalDocument

data class EditorDocument(
    val musicalDocument: MusicalDocument,
    val content: String
)
