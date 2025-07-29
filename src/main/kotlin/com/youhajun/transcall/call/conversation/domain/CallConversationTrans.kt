package com.youhajun.transcall.call.conversation.domain

import com.youhajun.transcall.common.domain.BaseEntity
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("call_conversation_trans")
data class CallConversationTrans(
    @Id
    @Column("id")
    val id: UUID? = null,
    @Column("room_code")
    val roomCode: UUID,
    @Column("conversation_id")
    val conversationId: UUID,
    @Column("receiver_id")
    val receiverId: UUID,
    @Column("translated_text")
    val translatedText: String,
    @Column("translated_language")
    val translatedLanguage: LanguageType,
) : BaseEntity()