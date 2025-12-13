package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.dto.RoomInfoWithParticipantsResponse
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitExists
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class CallRoomRepositoryCustomImpl(
    template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : CallRoomRepositoryCustom {

    private val select = template.select<CallRoom>()

    override suspend fun findAllCurrentRoomInfoWithParticipants(
        participantSort: SortDirection,
        createdAtSort: SortDirection,
        cursor: UUIDCursor?,
        limit: Int
    ): List<RoomInfoWithParticipantsResponse> {
        val createdAtDir = createdAtSort.name
        val participantsDir = participantSort.name

        val sql = """
        SELECT 
            r.*, 
            p.id AS p_id, p.room_id AS p_room_id, p.user_id AS p_user_id, 
            p.display_name AS p_display_name, p.profile_image_url AS p_profile_image_url, 
            p.language AS p_language, p.country AS p_country, p.left_at AS p_left_at, 
            p.created_at AS p_created_at, p.updated_at AS p_updated_at
        FROM (
            SELECT * FROM call_room 
            WHERE visibility = 'PUBLIC' 
              AND status IN ('WAITING', 'IN_PROGRESS')
              ${if (cursor != null) { if (createdAtSort == SortDirection.ASC) "AND id > :cursorId" else "AND id < :cursorId" } else ""}
            ORDER BY current_participants_count $participantsDir, id $createdAtDir
            LIMIT :limit
        ) r
        LEFT JOIN call_participant p ON r.id = p.room_id AND p.left_at IS NULL
        ORDER BY r.current_participants_count $participantsDir, r.id $createdAtDir, p.id DESC
    """.trimIndent()

        var spec = databaseClient.sql(sql).bind("limit", limit)
        cursor?.let { spec = spec.bind("cursorId", it.uuid) }

        val flatData = spec.map { row, _ ->
            val room = CallRoom.from(row)
            val participant = row.get("p_id", UUID::class.java)?.let { CallParticipant.from(row, "p_") }
            room to participant
        }.all().collectList().awaitSingle() ?: emptyList()

        return flatData
            .groupBy({ it.first }, { it.second })
            .map { (room, participants) ->
                RoomInfoWithParticipantsResponse(
                    roomInfo = room.toRoomInfoResponse(),
                    participants = participants.mapNotNull { it?.toParticipantResponse() }
                )
            }
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

    override suspend fun updateRoomStatusAndCount(roomId: UUID) {
        val sql = """
        UPDATE call_room
        SET 
            current_participants_count = sub.cnt,
            status = CASE 
                WHEN sub.cnt = 0 THEN :endedStatus
                WHEN sub.cnt = 1 THEN :waitingStatus
                ELSE :inProgressStatus
            END,
            updated_at = NOW()
        FROM (
            SELECT COUNT(*) as cnt 
            FROM call_participant 
            WHERE room_id = :roomId AND left_at IS NULL
        ) sub
        WHERE id = :roomId
    """.trimIndent()

        databaseClient.sql(sql)
            .bind("roomId", roomId)
            .bind("endedStatus", RoomStatus.ENDED.name)
            .bind("waitingStatus", RoomStatus.WAITING.name)
            .bind("inProgressStatus", RoomStatus.IN_PROGRESS.name)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}