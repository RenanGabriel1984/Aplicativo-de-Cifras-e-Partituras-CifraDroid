package com.example.domain.editor

data class EditorSelection(
    val start: EditorCursor,
    val end: EditorCursor,
    val selectedText: String = ""
)
