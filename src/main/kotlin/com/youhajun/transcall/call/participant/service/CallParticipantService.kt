package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.common.vo.TimeRange
import java.util.*

interface CallParticipantService {
    suspend fun checkParticipant(roomId: UUID, userId: UUID)
    suspend fun saveParticipant(roomId: UUID, userId: UUID): UUID
    suspend fun updateParticipantOnLeave(participantId: UUID)
    suspend fun findParticipantsGroupByRoomIds(roomIds: List<UUID>): Map<UUID, List<CallParticipant>>
    suspend fun findCurrentParticipantsGroupByRoomIds(roomIds: List<UUID>): Map<UUID, List<CallParticipant>>
    suspend fun currentCountByRoomId(roomId: UUID): Int
    suspend fun findCurrentParticipants(roomId: UUID): List<CallParticipant>
    suspend fun findParticipantsByRoomIdAndTimeRange(roomId: UUID, timeRange: TimeRange): List<CallParticipant>
}