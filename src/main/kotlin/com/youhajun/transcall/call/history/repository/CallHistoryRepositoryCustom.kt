package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import java.util.*

interface CallHistoryRepositoryCustom {
    suspend fun findPageByUserIdAndCursor(
        userId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallHistory>

    suspend fun updateCallHistoryOnLeave(historyId: UUID)
}