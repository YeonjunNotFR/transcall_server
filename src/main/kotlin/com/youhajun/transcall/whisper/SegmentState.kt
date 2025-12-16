package com.youhajun.transcall.whisper

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.user.domain.LanguageType
import com.youhajun.transcall.ws.dto.payload.TranslationMessage

data class SegmentState(
    val key: SegmentKey,
    val conversation: CallConversation,
    val accumulatedText: String = "",
    val lastBuffer: String = "",
    val isFinal: Boolean = false,
) {
    val originText = accumulatedText + lastBuffer

    fun toTranslationMessage(transText: String?, transLanguage: LanguageType?): TranslationMessage = TranslationMessage(
        message = conversation.toConversationResponse(
            transText = transText,
            transLanguage = transLanguage,
        ).copy(
            originText = originText
        )
    )
}