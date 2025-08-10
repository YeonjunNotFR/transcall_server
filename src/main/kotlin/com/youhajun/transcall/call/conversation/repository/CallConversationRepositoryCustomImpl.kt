package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class CallConversationRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallConversationRepositoryCustom {

    private val select = template.select<CallConversation>()

    override suspend fun findPageByRoomCodeAndCursor(
        roomId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallConversation> {
        val base = Criteria.where("room_id").`is`(roomId)
        val criteria = cursor?.let {
            base.and("id").lessThan(it.uuid)
        } ?: base

        return select
            .matching(
                Query.query(criteria)
                    .sort(Sort.by(Sort.Direction.DESC, "id"))
                    .limit(limit)
            )
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }
}