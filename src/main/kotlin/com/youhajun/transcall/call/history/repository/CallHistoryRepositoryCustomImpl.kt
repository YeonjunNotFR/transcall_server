package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.r2dbc.core.update
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class CallHistoryRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallHistoryRepositoryCustom {

    private val select = template.select<CallHistory>()
    private val update = template.update<CallHistory>()

    override suspend fun findPageByUserIdAndCursor(
        userId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallHistory> {
        val base = Criteria.where("user_id").`is`(userId)
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

    override suspend fun updateCallHistoryOnLeave(historyId: UUID) {
        val criteria = Criteria.where("id").`is`(historyId)
        update
            .matching(Query.query(criteria))
            .apply(Update.update("left_at", Instant.now()))
            .awaitSingleOrNull()
    }
}