package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomStatus
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import java.util.*

class CallRoomRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallRoomRepositoryCustom {

    private val select = template.select<CallRoom>()
    private val update = template.update<CallRoom>()

    override suspend fun existsByRoomCode(roomCode: String): Boolean {
        val criteria = Criteria.where("room_code").`is`(roomCode)
        return select
            .matching(Query.query(criteria))
            .awaitExists()
    }

    override suspend fun findByRoomCode(roomCode: String): CallRoom? {
        val criteria = Criteria.where("room_code").`is`(roomCode)
        return select
            .matching(Query.query(criteria))
            .awaitOneOrNull()
    }

    override suspend fun updateRoomStatus(roomId: UUID, status: RoomStatus): Boolean {
        val criteria = Criteria.where("id").`is`(roomId).and("status").not(status)
        val row = update
            .matching(Query.query(criteria))
            .apply(Update.update("status", status))
            .awaitSingleOrNull() ?: 0
        return row > 0
    }
}