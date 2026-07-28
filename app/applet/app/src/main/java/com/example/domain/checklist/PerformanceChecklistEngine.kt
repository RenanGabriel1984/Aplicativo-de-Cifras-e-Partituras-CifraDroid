package com.example.domain.checklist

import com.example.domain.performance.PerformancePreparation
import com.example.domain.performance.PerformanceStatus

object PerformanceChecklistEngine {

    fun createChecklist(preparation: PerformancePreparation): PerformanceChecklist {
        val items = generateItems(preparation)
        return PerformanceChecklist(
            performanceId = preparation.id,
            items = items,
            completionPercentage = calculateCompletion(items),
            pendingItems = pendingItems(items),
            warningItems = warningItems(items)
        )
    }

    fun generateItems(preparation: PerformancePreparation): List<ChecklistItem> {
        return preparation.songs.map { song ->
            val title = when (song.status) {
                PerformanceStatus.NOT_PREPARED -> "Pendente preparação"
                PerformanceStatus.PARTIALLY_PREPARED -> "Revisar preparação"
                PerformanceStatus.READY -> "Preparação concluída"
                PerformanceStatus.LOCKED -> "Documento bloqueado"
            }
            
            val status = when (song.status) {
                PerformanceStatus.NOT_PREPARED -> ChecklistStatus.PENDING
                PerformanceStatus.PARTIALLY_PREPARED -> ChecklistStatus.WARNING
                PerformanceStatus.READY -> ChecklistStatus.COMPLETED
                PerformanceStatus.LOCKED -> ChecklistStatus.COMPLETED
            }
            
            ChecklistItem(
                id = "item_song_${song.id}",
                title = title,
                description = "Preparação para a música ${song.songDocument.metadata.title}",
                category = ChecklistCategory.SONG,
                status = status,
                relatedSongId = song.id
            )
        }
    }

    fun calculateCompletion(items: List<ChecklistItem>): Float {
        if (items.isEmpty()) return 0f
        val completed = completedItems(items)
        return (completed.toFloat() / items.size.toFloat()) * 100f
    }

    fun pendingItems(items: List<ChecklistItem>): Int {
        return items.count { it.status == ChecklistStatus.PENDING }
    }

    fun warningItems(items: List<ChecklistItem>): Int {
        return items.count { it.status == ChecklistStatus.WARNING }
    }

    fun completedItems(items: List<ChecklistItem>): Int {
        return items.count { it.status == ChecklistStatus.COMPLETED }
    }
}
