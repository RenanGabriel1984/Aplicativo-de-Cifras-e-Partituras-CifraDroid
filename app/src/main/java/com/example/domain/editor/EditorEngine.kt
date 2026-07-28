package com.example.domain.editor

import com.example.domain.document.MusicalDocument

object EditorEngine {

    fun createEditor(musicalDocument: MusicalDocument, initialContent: String = ""): EditorState {
        return EditorState(
            document = EditorDocument(musicalDocument, initialContent),
            cursor = EditorCursor(0),
            selection = null
        )
    }

    fun moveCursor(state: EditorState, newIndex: Int): EditorState {
        val safeIndex = newIndex.coerceIn(0, state.document.content.length)
        return state.copy(
            cursor = EditorCursor(safeIndex),
            selection = null
        )
    }

    fun selectText(state: EditorState, start: Int, end: Int): EditorState {
        val safeStart = start.coerceIn(0, state.document.content.length)
        val safeEnd = end.coerceIn(0, state.document.content.length)
        val min = minOf(safeStart, safeEnd)
        val max = maxOf(safeStart, safeEnd)
        val selectedText = state.document.content.substring(min, max)

        return state.copy(
            cursor = EditorCursor(safeEnd),
            selection = EditorSelection(EditorCursor(safeStart), EditorCursor(safeEnd), selectedText)
        )
    }

    fun insertText(state: EditorState, text: String): EditorState {
        if (state.selection != null) {
            return replaceSelection(state, text)
        }
        val content = state.document.content
        val cursorIndex = state.cursor.index

        val newContent = content.substring(0, cursorIndex) + text + content.substring(cursorIndex)
        val newCursorIndex = cursorIndex + text.length

        return state.copy(
            document = state.document.copy(content = newContent),
            cursor = EditorCursor(newCursorIndex)
        )
    }

    fun removeText(state: EditorState, count: Int): EditorState {
        if (state.selection != null) {
            return replaceSelection(state, "")
        }
        val content = state.document.content
        val cursorIndex = state.cursor.index

        val startIndex = maxOf(0, cursorIndex - count)
        val newContent = content.substring(0, startIndex) + content.substring(cursorIndex)

        return state.copy(
            document = state.document.copy(content = newContent),
            cursor = EditorCursor(startIndex)
        )
    }

    fun replaceSelection(state: EditorState, text: String): EditorState {
        val selection = state.selection ?: return insertText(state, text)
        val content = state.document.content
        val min = minOf(selection.start.index, selection.end.index)
        val max = maxOf(selection.start.index, selection.end.index)

        val newContent = content.substring(0, min) + text + content.substring(max)
        val newCursorIndex = min + text.length

        return state.copy(
            document = state.document.copy(content = newContent),
            cursor = EditorCursor(newCursorIndex),
            selection = null
        )
    }

    fun updateDocument(state: EditorState, newMusicalDocument: MusicalDocument): EditorState {
        return state.copy(
            document = state.document.copy(musicalDocument = newMusicalDocument)
        )
    }
}
