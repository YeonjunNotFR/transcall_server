package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.JanusRequestBuilder
import com.youhajun.transcall.janus.JanusWebSocketClient
import com.youhajun.transcall.janus.dto.JanusSuccessResponse
import com.youhajun.transcall.ws.vo.JanusSessionInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingle
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class JanusServiceImpl(
    private val client: JanusWebSocketClient,
    @Qualifier("janusWebClient") private val httpClient: WebClient,
) : JanusService {

    private val logger = LogManager.getLogger(JanusServiceImpl::class.java)

    override suspend fun createSession(session: WebSocketSession): Result<Long> = runCatching {
        val request = JanusRequestBuilder<Unit>()
            .create()
            .build()

        val response = client.sendRequest(session, request) as? JanusSuccessResponse
        response?.data?.id ?: throw IllegalStateException("Janus 세션 생성 실패: 응답에 sessionId가 없습니다.")
    }

    override suspend fun attachPlugin(session: WebSocketSession, sessionId: Long, plugin: String): Result<Long> =
        runCatching {
            val request = JanusRequestBuilder<Unit>()
                .session(sessionId)
                .attach(plugin)
                .build()

            val response = client.sendRequest(session, request) as? JanusSuccessResponse
            response?.data?.id ?: throw IllegalStateException("Janus 플러그인($plugin) 연결 실패: 응답에 sender(handleId)가 없습니다.")
        }

    override suspend fun destroySession(session: WebSocketSession, sessionId: Long): Result<Unit> = runCatching {
        val request = JanusRequestBuilder<Unit>()
            .session(sessionId)
            .destroy()
            .build()

        client.sendRequest(session, request)
        logger.info("[Janus] Session destroyed: $sessionId")
    }.onFailure { e ->
        logger.error("[Janus] Session destruction failed: ${sessionId}, error: ${e.message}")
    }

    override fun startKeepAlive(session: JanusSessionInfo, scope: CoroutineScope) {
        scope.launch {
            while (isActive && session.session.isOpen) {
                try {
                    val request = JanusRequestBuilder<Unit>()
                        .session(session.sessionId)
                        .keepalive()
                        .build()

                    client.sendRequest(session.session, request)

                    delay(25_000L)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    logger.warn("[Janus] Keep-alive failed for session ${session.sessionId}: ${e.message}")

                    delay(5_000L)
                }
            }
        }
    }

    override suspend fun createHttpSession(): Result<Long> = runCatching {
        val request = JanusRequestBuilder<Unit>()
            .create()
            .build()


        val response = httpClient.post()
            .uri("")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JanusSuccessResponse::class.java)
            .awaitSingle()

        response.data?.id ?: throw IllegalStateException("세션 생성 실패")
    }

    override suspend fun attachHttpPlugin(sessionId: Long, plugin: String): Result<Long> = runCatching {
        val request = JanusRequestBuilder<Unit>()
            .session(sessionId)
            .attach(plugin)
            .build()

        val response = httpClient.post()
            .uri("/$sessionId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JanusSuccessResponse::class.java)
            .awaitSingle()

        response.data?.id ?: throw IllegalStateException("플러그인($plugin) 연결 실패")
    }

    override suspend fun destroyHttpSession(sessionId: Long): Result<Unit> = runCatching {
        val request = JanusRequestBuilder<Unit>()
            .session(sessionId)
            .destroy()
            .build()

        httpClient.post()
            .uri("/$sessionId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .toBodilessEntity()
            .awaitSingle()

        Unit
    }
}