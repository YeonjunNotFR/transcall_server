package com.youhajun.transcall.call.history.domain

import org.springframework.data.relational.core.mapping.Table
import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
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
    val userId: UUID?,
    @Column("title")
    val title: String,
    @Column("summary")
    val summary: String? = null,
    @Column("memo")
    val memo: String? = null,
    @Column("liked")
    val liked: Boolean = false,
    @Column("deleted")
    val deleted: Boolean = false,
    @Column("left_at")
    val leftAt: Instant? = null,
) : BaseUUIDEntity() {
    fun toHistoryResponse(participants: List<CallParticipant>): CallHistoryResponse =
        CallHistoryResponse(
            historyId = id.toString(),
            roomId = roomId.toString(),
            joinedAtToEpochTime = createdAt.epochSecond,
            leftAtToEpochTime = leftAt?.epochSecond,
            participants = participants.map { it.toDto() },
            title = title,
            summary = summary ?: "",
            memo = memo ?: "",
            isLiked = liked
        )
}
