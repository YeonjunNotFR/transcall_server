package com.youhajun.transcall.call.conversation.domain

import com.youhajun.transcall.common.domain.BaseEntity
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("call_conversation")
data class CallConversation(
    @Id
    @Column("id")
    val id: UUID? = null,
    @Column("room_code")
    val roomCode: UUID,
    @Column("sender_id")
    val senderId: UUID,
    @Column("origin_text")
    val originText: String,
    @Column("origin_language")
    val originLanguage: LanguageType,
): BaseEntity()