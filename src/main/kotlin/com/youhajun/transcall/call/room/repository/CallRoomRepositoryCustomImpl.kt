package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitExists
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query

class CallRoomRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallRoomRepositoryCustom {

    private val select = template.select<CallParticipant>()

    override suspend fun existsByRoomCode(roomCode: String): Boolean {
        val criteria = Criteria.where("room_code").`is`(roomCode)
        return select
            .matching(Query.query(criteria))
            .awaitExists()
    }
}