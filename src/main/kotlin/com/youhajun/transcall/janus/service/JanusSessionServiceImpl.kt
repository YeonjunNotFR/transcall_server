package com.youhajun.transcall.janus.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.auth.*
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.util.JanusTransactionHelper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class JanusSessionServiceImpl(
    private val transactionHelper: JanusTransactionHelper,
    private val objectMapper: ObjectMapper,
    @Qualifier("janusWebClient") private val client: WebClient,
) : JanusSessionService {

    private val logger: Logger = LogManager.getLogger(JanusSessionServiceImpl::class.java)

    override suspend fun createSession(session: WebSocketSession): Result<CreateSessionData> {
        val request = CreateSessionRequest()
        val response = transactionHelper.requestJanusResponse<CreateSessionResponse>(session, request)
        return response.map { it.data }
    }

    override suspend fun destroySession(session: WebSocketSession, sessionId: Long) {
        val request = DestroySessionRequest(sessionId = sessionId)
        transactionHelper.sendJanusMessage(session, request)
    }

    override suspend fun createManagerSession(): Result<CreateSessionData> = runCatching {
        val request = CreateSessionRequest()

        client.post()
            .uri("/janus")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .doOnNext { logger.info("Janus createManagerSession raw response:\n{}", it.toPrettyString()) }
            .map { transactionHelper.parseJanusResponse<CreateSessionResponse>(it, objectMapper).data }
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }.onFailure {
        logger.error(it)
    }

    override suspend fun destroyManagerSession(sessionId: Long) {
        val request = DestroySessionRequest(sessionId = sessionId)

        client.post()
            .uri("/janus")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }

    override suspend fun keepAlive(session: WebSocketSession, sessionId: Long) {
        val request = KeepAliveRequest(sessionId = sessionId)
        transactionHelper.sendJanusMessage(session, request)
    }

    override suspend fun keepAliveManager(sessionId: Long) = runCatching {
        val request = KeepAliveRequest(sessionId = sessionId)
        client.post()
            .uri("/janus/$sessionId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .doOnNext { logger.info("Janus KeepAliveManager raw response:\n{}", it.toPrettyString()) }
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }.onFailure {
        logger.error(it)
    }
}