package com.youhajun.transcall.client.janus.service

import com.youhajun.transcall.client.janus.dto.plugin.*
import org.springframework.web.reactive.socket.WebSocketSession

interface JanusPluginService {
    suspend fun attachPlugin(session: WebSocketSession, sessionId: Long, plugin: JanusPlugin): Result<AttachData>

    suspend fun attachManagerPlugin(sessionId: Long, plugin: JanusPlugin): Result<AttachData>

    suspend fun trickle(session: WebSocketSession, request: TrickleCandidateRequest<TrickleCandidateBody>)

    suspend fun trickleComplete(session: WebSocketSession, request: TrickleCandidateRequest<TrickleCompletedBody>)
}