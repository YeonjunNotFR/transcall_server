package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.common.vo.TimeRange
import java.util.*

interface CallParticipantRepositoryCustom {
    suspend fun existsByRoomIdAndUserId(roomId: UUID, userId: UUID): Boolean

    suspend fun findAllByRoomIds(roomIds: List<UUID>): List<CallParticipant>

    suspend fun findCurrentParticipantsByRoomIds(roomIds: List<UUID>): List<CallParticipant>

    suspend fun findCurrentParticipantsByRoomId(roomId: UUID): List<CallParticipant>

    suspend fun findAllByRoomIdAndTimeRange(roomId: UUID, timeRange: TimeRange): List<CallParticipant>

    suspend fun currentCountByRoomId(roomId: UUID): Int

    suspend fun updateParticipantOnLeave(participantId: UUID)
}