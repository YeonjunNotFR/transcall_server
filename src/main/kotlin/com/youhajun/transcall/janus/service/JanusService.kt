package com.youhajun.transcall.janus.service

import com.youhajun.transcall.ws.vo.JanusSessionInfo
import kotlinx.coroutines.CoroutineScope
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession

@Service
interface JanusService {

    suspend fun createSession(session: WebSocketSession): Result<Long>

    suspend fun attachPlugin(session: WebSocketSession, sessionId: Long, plugin: String): Result<Long>

    suspend fun destroySession(session: WebSocketSession, sessionId: Long): Result<Unit>

    suspend fun createHttpSession(): Result<Long>
    suspend fun attachHttpPlugin(sessionId: Long, plugin: String): Result<Long>
    suspend fun destroyHttpSession(sessionId: Long): Result<Unit>

    fun startKeepAlive(session: JanusSessionInfo, scope: CoroutineScope)
}