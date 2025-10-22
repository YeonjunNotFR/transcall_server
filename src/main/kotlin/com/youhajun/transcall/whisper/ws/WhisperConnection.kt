package com.youhajun.transcall.whisper.ws

import com.youhajun.transcall.whisper.dto.WhisperEvent
import kotlinx.coroutines.flow.SharedFlow
import org.springframework.web.reactive.socket.WebSocketSession

data class WhisperConnection(
    val session: WebSocketSession,
    val eventFlow: SharedFlow<WhisperEvent>,
)