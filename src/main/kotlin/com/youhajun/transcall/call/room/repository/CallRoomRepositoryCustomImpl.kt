package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.domain.RoomVisibility
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CallRoomRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : CallRoomRepositoryCustom {

    private val select = template.select<CallRoom>()
    private val update = template.update<CallRoom>()

    override suspend fun findOngoingRoomList(
        participantCountDirection: SortDirection,
        createdAtDirection: SortDirection,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallRoom> {
        val statusCriteria = Criteria.where("status").`is`(RoomStatus.WAITING)
            .or(Criteria.where("status").`is`(RoomStatus.IN_PROGRESS))

        val base = Criteria.where("visibility").`is`(RoomVisibility.PUBLIC)
            .and(statusCriteria)

        val criteria = cursor?.let {
            if(createdAtDirection == SortDirection.ASC) base.and("id").greaterThan(it.uuid)
            else base.and("id").lessThan(it.uuid)
        } ?: base

        val sort = Sort.by(createdAtDirection.toSpring(), "id")
            .and(Sort.by(participantCountDirection.toSpring(), "current_participants_count"))

        return select
            .matching(
                Query.query(criteria)
                    .sort(sort)
                    .limit(limit)
            )
            .all()
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }

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

    override suspend fun updateRoomStatus(roomId: UUID, status: RoomStatus) {
        val criteria = Criteria.where("id").`is`(roomId)
        update
            .matching(Query.query(criteria))
            .apply(Update.update("status", status))
            .awaitSingleOrNull()
    }

    override suspend fun updateCurrentParticipantCount(roomId: UUID, count: Int) {
        val criteria = Criteria.where("id").`is`(roomId)
        update
            .matching(Query.query(criteria))
            .apply(Update.update("current_participants_count", count))
            .awaitSingleOrNull()
    }

    private fun SortDirection.toSpring() = when (this) {
        SortDirection.ASC -> Sort.Direction.ASC
        SortDirection.DESC -> Sort.Direction.DESC
    }
}