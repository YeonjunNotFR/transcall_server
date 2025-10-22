package com.youhajun.transcall.client.openai.ws

import com.youhajun.transcall.client.openai.dto.OpenAiEvent
import kotlinx.coroutines.flow.SharedFlow
import org.springframework.web.reactive.socket.WebSocketSession

data class OpenAiConnection(
    val session: WebSocketSession,
    val eventFlow: SharedFlow<OpenAiEvent>,
)