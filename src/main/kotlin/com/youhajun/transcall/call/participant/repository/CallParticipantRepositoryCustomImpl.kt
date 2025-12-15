package com.youhajun.transcall.call.participant.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitExists
import org.springframework.data.r2dbc.core.select
import org.springframework.data.r2dbc.core.update
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class CallParticipantRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallParticipantRepositoryCustom {

    private val select = template.select<CallParticipant>()
    private val update = template.update<CallParticipant>()

    override suspend fun existsByRoomIdAndUserId(roomId: UUID, userId: UUID): Boolean {
        val criteria = Criteria.where("room_id").`is`(roomId).and("user_id").`is`(userId)
        return select
            .matching(Query.query(criteria))
            .awaitExists()
    }

    override suspend fun findAllCurrentParticipantsByRoomId(roomId: UUID): List<CallParticipant> {
        val criteria = Criteria.where("room_id").`is`(roomId).and("left_at").isNull
        return select
            .matching(Query.query(criteria))
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }

    override suspend fun currentCountByRoomId(roomId: UUID): Int {
        val criteria = Criteria.where("room_id").`is`(roomId).and("left_at").isNull
        return select
            .matching(Query.query(criteria))
            .count()
            .awaitSingleOrNull()
            ?.toInt() ?: 0
    }

    override suspend fun updateParticipantOnLeave(participantId: UUID) {
        val criteria = Criteria.where("id").`is`(participantId)
        update
            .matching(Query.query(criteria))
            .apply(Update.update("left_at", Instant.now()))
            .awaitSingleOrNull()
    }
}