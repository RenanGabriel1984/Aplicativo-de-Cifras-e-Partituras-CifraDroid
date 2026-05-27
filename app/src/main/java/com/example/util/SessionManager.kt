package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SessionRole {
    LEADER, FOLLOWER, STANDALONE
}

data class SyncEvent(
    val manuscriptId: Int,
    val pageIndex: Int,
    val timestamp: Long,
    val note: String? = null
)

/**
 * SessionManager prepares the architecture for future 'Choir/Maestro Mode'
 * allowing synchronization of pages across local Wi-Fi.
 */
object SessionManager {
    private val _currentRole = MutableStateFlow(SessionRole.STANDALONE)
    val currentRole: StateFlow<SessionRole> = _currentRole.asStateFlow()

    private val _syncEvents = MutableStateFlow<SyncEvent?>(null)
    val syncEvents: StateFlow<SyncEvent?> = _syncEvents.asStateFlow()

    fun setRole(role: SessionRole) {
        _currentRole.value = role
    }

    fun broadcastPageChange(manuscriptId: Int, pageIndex: Int, note: String? = null) {
        if (_currentRole.value == SessionRole.LEADER) {
            val event = SyncEvent(manuscriptId, pageIndex, System.currentTimeMillis(), note)
            _syncEvents.value = event
            SessionNetworkManager.broadcast(event) // Broadcast via TCP/WebSockets
        }
    }

    
    // To be called by network listener in FOLLOWER mode
    fun onReceivedSyncEvent(event: SyncEvent) {
        if (_currentRole.value == SessionRole.FOLLOWER) {
            _syncEvents.value = event
        }
    }
}
