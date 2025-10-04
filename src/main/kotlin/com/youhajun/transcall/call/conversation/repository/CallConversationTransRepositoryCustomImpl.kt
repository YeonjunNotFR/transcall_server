package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CallConversationTransRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallConversationTransRepositoryCustom {

    private val select = template.select<CallConversationTrans>()

    override suspend fun findByConversationIdsAndReceiverId(
        conversationIds: List<UUID>,
        receiverId: UUID
    ): List<CallConversationTrans> {
        val criteria = Criteria.where("conversation_id").`in`(conversationIds).and("receiver_id").`is`(receiverId)
        return select
            .matching(Query.query(criteria))
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }
}