package com.example.domain.performance

import com.example.domain.preparation.PreparationDestination

object PerformancePreparationEngine {

    fun createPerformance(
        id: String,
        title: String,
        category: PreparationDestination,
        date: Long,
        songs: List<PerformanceSong>
    ): PerformancePreparation {
        val stats = calculateStatistics(songs)
        val readi = calculateReadiness(stats.completionPercentage)
        
        return PerformancePreparation(
            id = id,
            title = title,
            category = category,
            date = date,
            songs = songs,
            statistics = stats,
            readiness = readi
        )
    }

    fun calculateStatistics(songs: List<PerformanceSong>): PerformanceStatistics {
        val totalSongs = songs.size
        val readySongs = songs.count { it.status == PerformanceStatus.READY || it.status == PerformanceStatus.LOCKED }
        val pendingSongs = totalSongs - readySongs
        val percentage = completionPercentage(readySongs, totalSongs)

        return PerformanceStatistics(
            totalSongs = totalSongs,
            readySongs = readySongs,
            pendingSongs = pendingSongs,
            completionPercentage = percentage
        )
    }

    fun calculateReadiness(completionPercentage: Float): PerformanceReadiness {
        return when {
            completionPercentage == 0f -> PerformanceReadiness.NOT_READY
            completionPercentage < 80f -> PerformanceReadiness.ALMOST_READY
            completionPercentage < 100f -> PerformanceReadiness.READY
            else -> PerformanceReadiness.PERFORMANCE_READY
        }
    }

    fun updateSongStatus(
        preparation: PerformancePreparation,
        songId: String,
        newStatus: PerformanceStatus
    ): PerformancePreparation {
        val updatedSongs = preparation.songs.map {
            if (it.id == songId) {
                it.copy(status = newStatus, lastReviewed = System.currentTimeMillis())
            } else {
                it
            }
        }
        val stats = calculateStatistics(updatedSongs)
        val readi = calculateReadiness(stats.completionPercentage)
        
        return preparation.copy(
            songs = updatedSongs,
            statistics = stats,
            readiness = readi
        )
    }

    fun completionPercentage(readySongs: Int, totalSongs: Int): Float {
        if (totalSongs == 0) return 0f
        return (readySongs.toFloat() / totalSongs.toFloat()) * 100f
    }
}
