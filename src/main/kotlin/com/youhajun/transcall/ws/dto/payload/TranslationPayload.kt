package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.call.conversation.dto.ConversationResponse

sealed interface TranslationResponse : ResponsePayload

data object SttStart : TranslationResponse {
    const val ACTION = "sttStart"
}

data class TranslationMessage(
    val message: ConversationResponse
) : TranslationResponse {
    companion object {
        const val ACTION = "transMessage"
    }
}