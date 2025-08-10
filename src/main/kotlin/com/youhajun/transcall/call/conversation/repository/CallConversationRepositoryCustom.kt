package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.pagination.cursor.CreatedAtCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CallConversationRepositoryCustom {

    suspend fun findPageByRoomCodeAndCursor(
        roomId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallConversation>
}