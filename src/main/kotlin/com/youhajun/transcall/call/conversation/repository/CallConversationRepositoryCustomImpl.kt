package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CallConversationRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallConversationRepositoryCustom {

    private val select = template.select<CallConversation>()

    override suspend fun findPageByTimeRangeAndCursorOldest(
        roomId: UUID,
        timeRange: TimeRange,
        cursor: UUIDCursor?,
        limit: Int,
    ): List<CallConversation> {
        val base = Criteria.where("room_id").`is`(roomId)
            .and("created_at").greaterThanOrEquals(timeRange.joinedAt)
            .let {
                if (timeRange.leftAt != null) it.and("created_at").lessThanOrEquals(timeRange.leftAt) else it
            }

        val criteria = cursor?.let { base.and("id").greaterThan(it.uuid) } ?: base

        return select
            .matching(
                Query.query(criteria)
                    .sort(Sort.by(Sort.Direction.ASC, "id"))
                    .limit(limit)
            )
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }

    override suspend fun findPageByTimeRangeSyncNewest(
        roomId: UUID,
        timeRange: TimeRange,
        cursor: UUIDCursor,
        limit: Int,
        updatedAfterEpochTime: Long
    ): List<CallConversation> {
        val criteria = Criteria.where("room_id").`is`(roomId)
            .and("id").lessThan(cursor.uuid)
            .and("updated_at").greaterThan(updatedAfterEpochTime)
            .and("created_at").greaterThanOrEquals(timeRange.joinedAt)
            .let {
                if (timeRange.leftAt != null) it.and("created_at").lessThanOrEquals(timeRange.leftAt) else it
            }

        return select
            .matching(
                Query.query(criteria).sort(Sort.by(Sort.Direction.DESC, "id")).limit(limit)
            )
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }

}