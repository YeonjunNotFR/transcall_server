package com.youhajun.transcall.call.conversation.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.domain.LanguageType
import io.r2dbc.spi.Row
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("call_conversation")
data class CallConversation(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("room_id")
    val roomId: UUID,
    @Column("sender_id")
    val senderId: UUID?,
    @Column("participant_id")
    val participantId: UUID?,
    @Column("origin_text")
    val originText: String,
    @Column("origin_language")
    val originLanguage: LanguageType,
    @Column("state")
    val state: ConversationState = ConversationState.PENDING,
) : BaseUUIDEntity() {

    fun toConversationResponse(
        transText: String?,
        transLanguage: LanguageType?,
    ) = ConversationResponse(
        conversationId = uuid.toString(),
        roomId = roomId.toString(),
        participantId = participantId?.toString(),
        senderId = senderId?.toString(),
        originText = originText,
        originLanguage = originLanguage,
        transText = transText,
        transLanguage = transLanguage,
        state = state,
        createdAt = createdAt.epochSecond,
        updatedAt = updatedAt.epochSecond,
    )

    companion object {
        fun from(row: Row): CallConversation = CallConversation(
            uuid = row.get("id", UUID::class.java)!!,
            roomId = row.get("room_id", UUID::class.java)!!,
            senderId = row.get("sender_id", UUID::class.java),
            participantId = row.get("participant_id", UUID::class.java),
            originText = row.get("origin_text", String::class.java)!!,
            originLanguage = LanguageType.fromCode(row.get("origin_language", String::class.java)!!),
            state = ConversationState.fromType(row.get("state", String::class.java)!!)
        ).apply {
            createdAt = row.get("created_at", Instant::class.java)!!
            updatedAt = row.get("updated_at", Instant::class.java)!!
        }
    }
}
