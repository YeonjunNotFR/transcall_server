package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import java.util.*

interface CallParticipantRepositoryCustom {
    suspend fun existsByRoomIdAndUserId(roomId: UUID, userId: UUID): Boolean

    suspend fun findAllCurrentParticipantsByRoomId(roomId: UUID): List<CallParticipant>

    suspend fun currentCountByRoomId(roomId: UUID): Int

    suspend fun updateParticipantOnLeave(participantId: UUID)
}