package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.participant.domain.CallParticipant
import java.util.*

interface CallParticipantService {
    suspend fun checkCallParticipant(roomId: UUID, userId: UUID)
    suspend fun joinCallParticipant(roomId: UUID, userId: UUID)
    suspend fun leaveCallParticipant(roomId: UUID, userId: UUID)
    suspend fun findCallParticipantsGroupedByRoomId(roomIds: List<UUID>): Map<UUID, List<CallParticipant>>
    suspend fun currentCountByRoomId(roomId: UUID): Int
    suspend fun findCurrentCallParticipants(roomId: UUID): List<CallParticipant>
}