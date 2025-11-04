package com.youhajun.transcall.call.conversation.dto

import com.youhajun.transcall.call.conversation.domain.ConversationState
import com.youhajun.transcall.user.domain.LanguageType

data class ConversationResponse(
    val conversationId: String,
    val roomId: String,
    val senderId: String?,
    val state: ConversationState,
    val originText: String,
    val originLanguage: LanguageType,
    val transText: String?,
    val transLanguage: LanguageType?,
    val createdAtToEpochTime: Long,
    val updatedAtToEpochTime: Long
)