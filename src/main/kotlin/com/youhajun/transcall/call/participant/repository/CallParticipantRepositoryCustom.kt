package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.pagination.cursor.CreatedAtCursor
import java.util.*

interface CallParticipantRepositoryCustom {
    suspend fun existsByRoomCodeAndUserId(roomCode: UUID, userId: UUID): Boolean

    suspend fun findPageByUserPublicIdAndCursor(
        userPublicId: UUID,
        cursor: CreatedAtCursor?,
        limit: Int
    ): List<CallParticipant>
}