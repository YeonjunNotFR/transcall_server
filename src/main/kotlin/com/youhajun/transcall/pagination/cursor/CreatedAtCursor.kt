package com.youhajun.transcall.pagination.cursor

import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

data class CreatedAtCursor(
    val createdAt: LocalDateTime,
    val id: Long
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
            id = idStr.toLong()
        )
    }
}