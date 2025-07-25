package com.youhajun.transcall.pagination.cursor

import org.springframework.data.relational.core.query.Criteria
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

data class CreatedAtCursor(
    val createdAt: LocalDateTime,
    val id: UUID
) : Cursor

object CreatedAtCursorCodec : CursorCodec<CreatedAtCursor> {
    private val encoder = Base64.getUrlEncoder()
    private val decoder = Base64.getUrlDecoder()

    override fun encode(cursor: CreatedAtCursor): String {
        val raw = "${cursor.createdAt}|${cursor.id}"
        return encoder.encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
    }

    override fun decode(raw: String): CreatedAtCursor {
        val decoded = String(decoder.decode(raw), StandardCharsets.UTF_8)
        val (createdAtStr, idStr) = decoded.split("|")
        return CreatedAtCursor(
            createdAt = LocalDateTime.parse(createdAtStr),
            id = UUID.fromString(idStr)
        )
    }
}

fun andCreatedAtCursor(
    base: Criteria,
    cursor: CreatedAtCursor?
): Criteria {
    if (cursor == null) return base

    val cursorCondition = Criteria
        .where("created_at").greaterThan(cursor.createdAt)
        .or(
            Criteria.where("created_at").`is`(cursor.createdAt)
                .and(Criteria.where("id").greaterThan(cursor.id))
        )

    return base.and(cursorCondition)
}