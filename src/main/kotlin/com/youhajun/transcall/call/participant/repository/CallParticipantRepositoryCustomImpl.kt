package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.pagination.cursor.CreatedAtCursor
import com.youhajun.transcall.pagination.cursor.andCreatedAtCursor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitExists
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class CallParticipantRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallParticipantRepositoryCustom {

    private val select = template.select<CallParticipant>()

    override suspend fun existsByRoomCodeAndUserId(roomCode: UUID, userId: UUID): Boolean {
        val criteria = Criteria.where("room_code").`is`(roomCode).and("user_public_id").`is`(userId)
        return select
            .matching(Query.query(criteria))
            .awaitExists()
    }

    override suspend fun findPageByUserPublicIdAndCursor(
        userPublicId: UUID,
        cursor: CreatedAtCursor?,
        limit: Int
    ): List<CallParticipant> {
        val base = Criteria.where("user_public_id").`is`(userPublicId)
        val criteria = andCreatedAtCursor(base, cursor)

        return select
            .matching(
                Query.query(criteria)
                    .sort(Sort.by(Sort.Direction.ASC, "created_at"))
                    .limit(limit)
            )
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }
}