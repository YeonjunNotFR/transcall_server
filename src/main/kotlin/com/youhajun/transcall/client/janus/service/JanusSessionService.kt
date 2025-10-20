package com.youhajun.transcall.client.janus.service

import com.youhajun.transcall.client.janus.dto.auth.CreateSessionData
import org.springframework.web.reactive.socket.WebSocketSession

interface JanusSessionService {
    suspend fun createSession(session: WebSocketSession): Result<CreateSessionData>

    suspend fun destroySession(session: WebSocketSession, sessionId: Long)

    suspend fun createManagerSession(): Result<CreateSessionData>

    suspend fun destroyManagerSession(sessionId: Long)

    suspend fun keepAlive(session: WebSocketSession, sessionId: Long)
}