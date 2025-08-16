package com.youhajun.transcall.call.conversation.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("call_conversation_trans")
data class CallConversationTrans(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("room_id")
    val roomId: UUID,
    @Column("conversation_id")
    val conversationId: UUID,
    @Column("receiver_id")
    val receiverId: UUID?,
    @Column("translated_text")
    val translatedText: String,
    @Column("translated_language")
    val translatedLanguage: LanguageType,
) : BaseUUIDEntity()