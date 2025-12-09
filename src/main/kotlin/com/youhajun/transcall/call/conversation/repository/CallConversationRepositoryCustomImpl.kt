package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.call.conversation.domain.ConversationState
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.user.domain.LanguageType
import io.r2dbc.spi.Row
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
class CallConversationRepositoryCustomImpl(
    template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : CallConversationRepositoryCustom {

    private val select = template.select<CallConversation>()
    private val update = template.update<CallConversation>()

    override suspend fun findAllByTimeRangeAndCursorOldest(
        roomId: UUID,
        timeRange: TimeRange,
        cursor: UUIDCursor?,
        limit: Int
    ): List<ConversationResponse> {
        val sql = """
        SELECT 
            c.*, 
            t.translated_text, 
            t.translated_language
        FROM call_conversation c
        INNER JOIN call_participant p ON c.participant_id = p.id
        LEFT JOIN call_conversation_trans t 
            ON c.id = t.conversation_id 
            AND t.translated_language = p.language
        WHERE c.room_id = :roomId
          AND c.created_at >= :joinedAt
          ${if (timeRange.leftAt != null) "AND c.created_at <= :leftAt" else ""}
          ${if (cursor != null) "AND c.id > :cursorId" else ""}
        ORDER BY c.id ASC
        LIMIT :limit
    """.trimIndent()

        var spec = databaseClient.sql(sql)
            .bind("roomId", roomId)
            .bind("joinedAt", timeRange.joinedAt)
            .bind("limit", limit)

        timeRange.leftAt?.let { spec = spec.bind("leftAt", it) }
        cursor?.let { spec = spec.bind("cursorId", it.uuid) }

        return spec.map { row, _ ->
            mapRowToResponse(row)
        }.all().collectList().awaitSingle() ?: emptyList()
    }

    override suspend fun findAllByTimeRangeAndUpdatedAfterOldest(
        roomId: UUID,
        timeRange: TimeRange,
        updatedAfter: Long
    ): List<ConversationResponse> {
        val updatedAfterInstant = Instant.ofEpochMilli(updatedAfter)

        val sql = """
        SELECT 
            c.*, 
            t.translated_text, 
            t.translated_language
        FROM call_conversation c
        INNER JOIN call_participant p ON c.participant_id = p.id
        LEFT JOIN call_conversation_trans t 
            ON c.id = t.conversation_id 
            AND t.translated_language = p.language
        WHERE c.room_id = :roomId
          AND c.created_at >= :joinedAt
          ${if (timeRange.leftAt != null) "AND c.created_at <= :leftAt" else ""}
          AND c.updated_at > :updatedAfter
        ORDER BY c.id ASC
    """.trimIndent()

        var spec = databaseClient.sql(sql)
            .bind("roomId", roomId)
            .bind("joinedAt", timeRange.joinedAt)
            .bind("updatedAfter", updatedAfterInstant)

        timeRange.leftAt?.let { spec = spec.bind("leftAt", it) }

        return spec.map { row, _ ->
            mapRowToResponse(row)
        }.all().collectList().awaitSingle() ?: emptyList()
    }

    override suspend fun updateConversationOriginText(conversationId: UUID, originText: String) {
        val criteria = Criteria.where("id").`is`(conversationId)
        val query = Update.update("origin_text", originText).set("state", ConversationState.FINAL)

        update
            .matching(Query.query(criteria))
            .apply(query)
            .awaitSingleOrNull()
    }

    private fun mapRowToResponse(row: Row): ConversationResponse {
        val transText = row.get("translated_text", String::class.java)
        val transLanguage = row.get("translated_language", String::class.java)?.let { LanguageType.fromCode(it) }
        return CallConversation
            .from(row)
            .toConversationResponse(transText, transLanguage)
    }

}