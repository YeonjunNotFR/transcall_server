package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import java.util.*

interface CallConversationRepositoryCustom {

    suspend fun findAllByTimeRangeAndCursorOldest(
        roomId: UUID,
        timeRange: TimeRange,
        cursor: UUIDCursor?,
        limit: Int,
    ): List<ConversationResponse>

    suspend fun findAllByTimeRangeAndUpdatedAfterOldest(
        roomId: UUID,
        timeRange: TimeRange,
        updatedAfter: Long,
    ): List<ConversationResponse>

    suspend fun updateConversationOriginText(conversationId: UUID, originText: String)
}