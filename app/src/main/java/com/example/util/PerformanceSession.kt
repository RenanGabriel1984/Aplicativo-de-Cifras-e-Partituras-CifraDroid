package com.example.util

import kotlinx.coroutines.flow.asStateFlow

data class PerformanceSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startedAt: Long = System.currentTimeMillis(),
    val elapsedTime: Long = 0L,
    val songsPlayed: Int = 0,
    val executedRelationships: Int = 0,
    val currentSong: String = "",
    val currentPass: String = "",
    val focusModeEnabled: Boolean = false,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

object PerformanceSessionManager {
    private val _session = kotlinx.coroutines.flow.MutableStateFlow<PerformanceSession?>(null)
    val session: kotlinx.coroutines.flow.StateFlow<PerformanceSession?> = _session.asStateFlow()

    fun startSession(songName: String) {
        _session.value = PerformanceSession(
            currentSong = songName,
            focusModeEnabled = true,
            isRunning = true,
            startedAt = System.currentTimeMillis()
        )
    }

    fun stopSession() {
        _session.value = _session.value?.copy(isRunning = false, isFinished = true)
    }

    fun pauseSession() {
        _session.value = _session.value?.copy(isRunning = false)
    }

    fun resumeSession() {
        _session.value = _session.value?.copy(isRunning = true)
    }
    
    fun clearSession() {
        _session.value = null
    }

    fun updateTime(elapsedMillis: Long) {
        _session.value = _session.value?.copy(elapsedTime = elapsedMillis)
    }

    fun incrementSongsPlayed() {
        _session.value = _session.value?.let { it.copy(songsPlayed = it.songsPlayed + 1) }
    }

    fun incrementRelationships() {
        _session.value = _session.value?.let { it.copy(executedRelationships = it.executedRelationships + 1) }
    }

    fun updateCurrentSong(songName: String) {
         _session.value = _session.value?.copy(currentSong = songName)
    }

    fun updateCurrentPass(pass: String) {
         _session.value = _session.value?.copy(currentPass = pass)
    }
}
