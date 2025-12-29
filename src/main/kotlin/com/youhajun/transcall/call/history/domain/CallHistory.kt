package com.youhajun.transcall.call.history.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import io.r2dbc.spi.Row
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("call_history")
data class CallHistory(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("room_id")
    val roomId: UUID,
    @Column("user_id")
    val userId: UUID,
    @Column("title")
    val title: String,
    @Column("summary")
    val summary: String = "",
    @Column("memo")
    val memo: String = "",
    @Column("liked")
    val liked: Boolean = false,
    @Column("deleted")
    val deleted: Boolean = false,
    @Column("left_at")
    val leftAt: Instant? = null,
) : BaseUUIDEntity() {
    fun toHistoryResponse(): CallHistoryResponse =
        CallHistoryResponse(
            historyId = id.toString(),
            roomId = roomId.toString(),
            title = title,
            summary = summary,
            memo = memo,
            isLiked = liked,
            leftAt = leftAt?.toEpochMilli(),
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli(),
        )

    companion object {
        fun from(row: Row): CallHistory {
            return CallHistory(
                uuid = row.get("id", UUID::class.java)!!,
                roomId = row.get("room_id", UUID::class.java)!!,
                userId = row.get("user_id", UUID::class.java)!!,
                title = row.get("title", String::class.java)!!,
                summary = row.get("summary", String::class.java)!!,
                memo = row.get("memo", String::class.java)!!,
                liked = row.get("liked", Boolean::class.javaObjectType)!!,
                deleted = row.get("deleted", Boolean::class.javaObjectType)!!,
                leftAt = row.get("left_at", Instant::class.java)
            ).apply {
                createdAt = row.get("created_at", Instant::class.java)!!
                updatedAt = row.get("updated_at", Instant::class.java)!!
            }
        }
    }
}
