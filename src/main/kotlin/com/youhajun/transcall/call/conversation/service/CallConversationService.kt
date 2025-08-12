package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.pagination.dto.CursorPage
import java.util.*

interface CallConversationService {
    suspend fun getCallConversations(
        userId: UUID,
        roomId: UUID,
        after: String?,
        first: Int
    ): CursorPage<ConversationResponse>
}
