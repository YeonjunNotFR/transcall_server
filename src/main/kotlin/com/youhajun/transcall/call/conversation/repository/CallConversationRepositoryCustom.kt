package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import java.util.*

interface CallConversationRepositoryCustom {

    suspend fun findPageByTimeRangeAndCursorOldest(
        roomId: UUID,
        timeRange: TimeRange,
        cursor: UUIDCursor?,
        limit: Int,
    ): List<CallConversation>

    suspend fun findPageByTimeRangeSyncNewest(
        roomId: UUID,
        timeRange: TimeRange,
        cursor: UUIDCursor,
        limit: Int,
        updatedAfterEpochTime: Long,
    ): List<CallConversation>
}