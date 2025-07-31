package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class CallHistoryRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallHistoryRepositoryCustom {

    private val select = template.select<CallHistory>()

    override suspend fun findByRoomCodeIn(roomCodes: List<UUID>): List<CallHistory> {
        val criteria = Criteria.where("room_code").`in`(roomCodes)
        return select.matching(Query.query(criteria))
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }
}