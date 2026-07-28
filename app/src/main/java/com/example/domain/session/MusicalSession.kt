package com.example.domain.session

import com.example.domain.identity.MusicalIdentity

data class MusicalSession(
    val id: String,
    val title: String,
    val type: SessionType,
    val date: Long,
    val participants: List<SessionParticipant>,
    val songs: List<MusicalIdentity>,
    val statistics: SessionStatistics,
    val status: SessionStatus
)
