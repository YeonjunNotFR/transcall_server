package com.youhajun.transcall.call.conversation.dto

import com.youhajun.transcall.user.domain.LanguageType
import java.util.*

data class ConversationResponse(
    val conversationId: UUID,
    val senderId: UUID,
    val originText: String,
    val originLanguage: LanguageType,
    val translatedText: String,
    val translatedLanguage: LanguageType,
    val createdAtToEpochTime: Long
)