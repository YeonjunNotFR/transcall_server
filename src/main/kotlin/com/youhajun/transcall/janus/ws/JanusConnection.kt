package com.youhajun.transcall.janus.ws

import com.youhajun.transcall.janus.dto.event.JanusEvent
import kotlinx.coroutines.flow.SharedFlow
import org.springframework.web.reactive.socket.WebSocketSession

data class JanusConnection(
    val session: WebSocketSession,
    val eventFlow: SharedFlow<JanusEvent>,
)