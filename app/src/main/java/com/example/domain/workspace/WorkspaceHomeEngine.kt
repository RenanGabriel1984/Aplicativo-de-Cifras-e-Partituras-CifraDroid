package com.example.domain.workspace

import com.example.domain.models.SongDocument

object WorkspaceHomeEngine {

    fun buildWorkspaceHome(
        workspace: MusicalWorkspace,
        todaySummary: String,
        continueReading: SongDocument?,
        favorites: List<SongDocument>,
        recentSongs: List<SongDocument>,
        repertoires: List<WorkspaceCollection>,
        shortcuts: List<WorkspaceShortcut>
    ): WorkspaceHomeState {
        val stats = WorkspaceEngine.calculateStatistics(workspace).statistics
        
        return WorkspaceHomeState(
            currentWorkspace = workspace,
            todaySummary = todaySummary,
            continueReading = continueReading,
            favorites = favorites,
            recentSongs = recentSongs,
            repertoires = repertoires,
            shortcuts = shortcuts,
            statistics = stats
        )
    }
}
