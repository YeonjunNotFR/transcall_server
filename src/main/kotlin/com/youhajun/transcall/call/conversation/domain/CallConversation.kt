package com.youhajun.transcall.call.conversation.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
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
        state = state,
        senderId = senderId?.toString(),
        originText = originText,
        originLanguage = originLanguage,
        transText = transText,
        transLanguage = transLanguage,
        createdAtToEpochTime = createdAt.epochSecond,
        updatedAtToEpochTime = updatedAt.epochSecond,
    )
}
