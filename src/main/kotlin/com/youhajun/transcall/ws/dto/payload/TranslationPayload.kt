package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.user.domain.LanguageType

sealed interface TranslationRequest : RequestPayload

sealed interface TranslationResponse : ResponsePayload

data object SttStart : TranslationResponse {
    const val ACTION = "sttStart"
}

data class TranslationMessage(
    val conversationId: String,
    val roomId: String,
    val senderId: String?,
    val originText: String,
    val originLanguage: LanguageType,
    val transText: String? = null,
    val transLanguage: LanguageType? = null,
    val createdAtToEpochTime: Long,
) : TranslationResponse {
    companion object {
        const val ACTION = "transMessage"
    }
}