package com.example.domain.workspace

enum class ShortcutAction {
    NAVIGATE,
    CREATE,
    PLAY,
    SEARCH
}

data class WorkspaceShortcut(
    val title: String,
    val icon: String,
    val action: ShortcutAction,
    val priority: Int
)
