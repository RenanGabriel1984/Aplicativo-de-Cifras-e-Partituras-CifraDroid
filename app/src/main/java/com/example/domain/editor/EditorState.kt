package com.example.domain.editor

data class EditorState(
    val document: EditorDocument,
    val cursor: EditorCursor,
    val selection: EditorSelection? = null
)
