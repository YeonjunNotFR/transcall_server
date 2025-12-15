package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.participant.domain.CallParticipant
import java.util.*

interface CallParticipantService {
    suspend fun checkParticipant(roomId: UUID, userId: UUID)
    suspend fun saveParticipant(roomId: UUID, userId: UUID): UUID
    suspend fun updateParticipantOnLeave(participantId: UUID)
    suspend fun getCurrentCount(roomId: UUID): Int
    suspend fun getCurrentParticipants(roomId: UUID): List<CallParticipant>
}