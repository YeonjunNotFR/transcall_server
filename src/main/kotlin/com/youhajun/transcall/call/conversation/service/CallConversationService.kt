package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.domain.ConversationCache
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import kotlinx.coroutines.flow.Flow
import java.util.*

interface CallConversationService {

    suspend fun getConversationsInTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        pagination: CursorPagination,
    ): CursorPage<ConversationResponse>

    suspend fun getConversationsSyncTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        pagination: CursorPagination,
        updatedAfter: Long?,
    ): CursorPage<ConversationResponse>

    suspend fun publishConversationCache(roomId: UUID, userId: UUID, cache: ConversationCache)

    suspend fun subscribeConversationCache(roomId: UUID, userId: UUID): Flow<ConversationCache>
}
