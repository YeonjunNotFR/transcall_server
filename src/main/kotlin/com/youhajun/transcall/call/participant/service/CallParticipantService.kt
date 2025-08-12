package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.participant.domain.CallParticipant
import java.util.*

interface CallParticipantService {
    suspend fun checkCallParticipant(userId: UUID, roomId: UUID)
    suspend fun joinCallParticipant(userId: UUID, roomId: UUID)
    suspend fun findCallParticipantsGroupedByRoomId(roomIds: List<UUID>): Map<UUID, List<CallParticipant>>
}