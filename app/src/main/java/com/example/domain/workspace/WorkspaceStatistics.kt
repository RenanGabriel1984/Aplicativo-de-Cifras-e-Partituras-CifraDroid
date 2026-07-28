package com.example.domain.workspace

data class WorkspaceStatistics(
    val totalSongs: Int,
    val favorites: Int,
    val recentSongs: Int,
    val repertoires: Int,
    val hoursStudied: Float,
    val lastAccess: Long
)
