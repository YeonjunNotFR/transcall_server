package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.dto.plugin.*
import org.springframework.web.reactive.socket.WebSocketSession

interface JanusPluginService {
    suspend fun attachPlugin(
        session: WebSocketSession,
        sessionId: Long,
        plugin: JanusPlugin
    ): Result<AttachData>

    suspend fun attachManagerPlugin(
        sessionId: Long,
        plugin: JanusPlugin
    ): Result<AttachData>

    // ▼ DTO 넘기지 않고, 값으로 받는다
    suspend fun trickle(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    )

    suspend fun trickleComplete(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long
    )
}