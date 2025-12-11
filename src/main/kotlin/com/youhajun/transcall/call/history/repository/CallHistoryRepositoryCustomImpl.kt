package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import com.youhajun.transcall.call.history.dto.CallHistoryWithParticipantsResponse
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.r2dbc.core.update
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class CallHistoryRepositoryCustomImpl(
    template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : CallHistoryRepositoryCustom {

    private val select = template.select<CallHistory>()
    private val update = template.update<CallHistory>()

    override suspend fun findAllCallHistoryWithParticipants(
        userId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallHistoryWithParticipantsResponse> {
        val sql = """
        SELECT 
            h.*,
            p.id AS p_id, p.room_id AS p_room_id, p.user_id AS p_user_id, 
            p.display_name AS p_display_name, p.profile_image_url AS p_profile_image_url, 
            p.language AS p_language, p.country AS p_country, p.left_at AS p_left_at, 
            p.created_at AS p_created_at, p.updated_at AS p_updated_at
        FROM (
            SELECT * FROM call_history 
            WHERE user_id = :userId AND deleted = false
            ${if (cursor != null) "AND id < :cursorId" else ""}
            ORDER BY id DESC
            LIMIT :limit
        ) h
        LEFT JOIN call_participant p ON h.room_id = p.room_id 
            AND p.created_at <= COALESCE(h.left_at, NOW())
            AND (p.left_at IS NULL OR p.left_at >= h.created_at)
        ORDER BY h.id DESC
    """.trimIndent()

        var spec = databaseClient.sql(sql)
            .bind("userId", userId)
            .bind("limit", limit)
        cursor?.let { spec = spec.bind("cursorId", it.uuid) }

        val flatData = spec.map { row, _ ->
            val history = CallHistory.from(row)
            val participant = row.get("p_id", UUID::class.java)?.let { CallParticipant.from(row, "p_") }
            history to participant
        }.all().collectList().awaitSingle() ?: emptyList()

        return flatData
            .groupBy { it.first }
            .map { (history, pairs) ->
                CallHistoryWithParticipantsResponse(
                    history = history.toHistoryResponse(),
                    participants = pairs
                        .mapNotNull { it.second?.toParticipantResponse() }
                        .distinctBy { it.userId }
                )
            }
    }

    override suspend fun findCallHistoryWithParticipants(historyId: UUID, userId: UUID): CallHistoryWithParticipantsResponse? {
        val sql = """
        SELECT 
            h.*,
            p.id AS p_id, p.room_id AS p_room_id, p.user_id AS p_user_id, 
            p.display_name AS p_display_name, p.profile_image_url AS p_profile_image_url, 
            p.language AS p_language, p.country AS p_country, p.left_at AS p_left_at, 
            p.created_at AS p_created_at, p.updated_at AS p_updated_at
        FROM (
            SELECT * FROM call_history 
            WHERE id = :historyId AND deleted = false AND user_id = :userId    
        ) h
        LEFT JOIN call_participant p ON h.room_id = p.room_id 
            AND p.created_at <= COALESCE(h.left_at, NOW())
            AND (p.left_at IS NULL OR p.left_at >= h.created_at)
        ORDER BY p.id
    """.trimIndent()

        val rows = databaseClient.sql(sql)
            .bind("historyId", historyId)
            .bind("userId", userId)
            .map { row, _ ->
                CallHistory.from(row) to row.get("p_id", UUID::class.java)?.let { CallParticipant.from(row, "p_") }
            }
            .all()
            .collectList()
            .awaitSingle() ?: emptyList()

        return rows.firstOrNull()?.let { (history, _) ->
            CallHistoryWithParticipantsResponse(
                history = history.toHistoryResponse(),
                participants = rows.mapNotNull { it.second?.toParticipantResponse() }.distinctBy { it.userId }
            )
        }
    }

    override suspend fun updateCallHistoryOnLeave(historyId: UUID) {
        val criteria = Criteria.where("id").`is`(historyId)
        update
            .matching(Query.query(criteria))
            .apply(Update.update("left_at", Instant.now()))
            .awaitSingleOrNull()
    }
}