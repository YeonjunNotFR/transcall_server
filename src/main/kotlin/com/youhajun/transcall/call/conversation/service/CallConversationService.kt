package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import com.youhajun.transcall.user.domain.LanguageType
import java.util.*

interface CallConversationService {

    suspend fun getConversationsInTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        pagination: CursorPagination,
    ): CursorPage<ConversationResponse>

    suspend fun getConversationsSyncInTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        updatedAfter: Long,
    ): CursorPage<ConversationResponse>

    suspend fun saveConversation(
        roomId: UUID,
        participantId: UUID,
        senderId: UUID,
        originText: String,
        originLanguage: LanguageType
    ): CallConversation

    suspend fun updateConversationText(conversationId: UUID, text: String)

}
