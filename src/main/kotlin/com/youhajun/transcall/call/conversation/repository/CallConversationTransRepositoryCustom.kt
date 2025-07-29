package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CallConversationTransRepositoryCustom {

    suspend fun findByConversationIdsAndReceiverId(conversationIds: List<UUID>, receiverId: UUID): List<CallConversationTrans>
}