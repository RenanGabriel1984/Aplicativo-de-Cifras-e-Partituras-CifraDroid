package com.example.domain.session

import com.example.domain.identity.MusicalIdentity

object MusicalSessionEngine {

    fun createSession(
        id: String,
        title: String,
        type: SessionType,
        date: Long
    ): MusicalSession {
        val initialStatistics = calculateStatistics(emptyList(), emptyList())
        return MusicalSession(
            id = id,
            title = title,
            type = type,
            date = date,
            participants = emptyList(),
            songs = emptyList(),
            statistics = initialStatistics,
            status = SessionStatus.PLANNED
        )
    }

    fun addSong(session: MusicalSession, song: MusicalIdentity): MusicalSession {
        val newSongs = session.songs + song
        return session.copy(
            songs = newSongs,
            statistics = calculateStatistics(newSongs, session.participants)
        )
    }

    fun removeSong(session: MusicalSession, identityId: String): MusicalSession {
        val newSongs = session.songs.filter { it.id != identityId }
        return session.copy(
            songs = newSongs,
            statistics = calculateStatistics(newSongs, session.participants)
        )
    }

    fun changeSongOrder(session: MusicalSession, identityId: String, newIndex: Int): MusicalSession {
        val song = session.songs.find { it.id == identityId } ?: return session
        val currentSongs = session.songs.toMutableList()
        currentSongs.remove(song)
        
        val safeIndex = newIndex.coerceIn(0, currentSongs.size)
        currentSongs.add(safeIndex, song)
        
        return session.copy(songs = currentSongs)
    }

    fun addParticipant(session: MusicalSession, participant: SessionParticipant): MusicalSession {
        val newParticipants = session.participants + participant
        return session.copy(
            participants = newParticipants,
            statistics = calculateStatistics(session.songs, newParticipants)
        )
    }

    fun removeParticipant(session: MusicalSession, participantId: String): MusicalSession {
        val newParticipants = session.participants.filter { it.id != participantId }
        return session.copy(
            participants = newParticipants,
            statistics = calculateStatistics(session.songs, newParticipants)
        )
    }

    fun calculateStatistics(songs: List<MusicalIdentity>, participants: List<SessionParticipant>): SessionStatistics {
        return SessionStatistics(
            totalSongs = songs.size,
            totalParticipants = participants.size
        )
    }

    fun finishSession(session: MusicalSession): MusicalSession {
        return session.copy(status = SessionStatus.FINISHED)
    }
}
