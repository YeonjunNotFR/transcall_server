package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.dto.CallHistoryWithParticipantsResponse
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import java.util.*

interface CallHistoryRepositoryCustom {
    suspend fun findAllCallHistoryWithParticipants(
        userId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallHistoryWithParticipantsResponse>

    suspend fun findCallHistoryWithParticipants(
        historyId: UUID,
        userId: UUID
    ): CallHistoryWithParticipantsResponse?

    suspend fun updateCallHistoryOnLeave(historyId: UUID)
}