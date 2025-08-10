package com.youhajun.transcall.pagination.cursor

import org.springframework.data.relational.core.query.Criteria
import java.nio.charset.StandardCharsets
import java.util.*

data class UUIDCursor(
    val uuid: UUID,
) : Cursor

object UUIDCursorCodec : CursorCodec<UUIDCursor> {
    private val encoder = Base64.getUrlEncoder()
    private val decoder = Base64.getUrlDecoder()

    override fun encode(cursor: UUIDCursor): String {
        return encoder.encodeToString(cursor.uuid.toString().toByteArray(StandardCharsets.UTF_8))
    }

    override fun decode(raw: String): UUIDCursor {
        val decoded = String(decoder.decode(raw), StandardCharsets.UTF_8)
        return UUIDCursor(uuid = UUID.fromString(decoded),)
    }
}