package com.youhajun.transcall.janus.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.plugin.*
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.util.JanusTransactionHelper
import com.youhajun.transcall.janus.util.janusResponseMapper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class JanusPluginServiceImpl(
    private val transactionHelper: JanusTransactionHelper,
    private val objectMapper: ObjectMapper,
    @Qualifier("janusWebClient") private val client: WebClient,
) : JanusPluginService {

    private val logger: Logger = LogManager.getLogger(JanusPluginServiceImpl::class.java)

    override suspend fun attachPlugin(
        session: WebSocketSession,
        sessionId: Long,
        plugin: JanusPlugin
    ): Result<AttachData> {
        val request = AttachRequest(plugin = plugin.pkgName, sessionId = sessionId)
        val response = transactionHelper.requestJanusResponse<AttachResponse>(session, request)
        return response.map { it.data }
    }

    override suspend fun attachManagerPlugin(sessionId: Long, plugin: JanusPlugin): Result<AttachData> = runCatching {
        val request = AttachRequest(plugin = plugin.pkgName, sessionId = sessionId)

        client.post()
            .uri("/janus")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .map { it.janusResponseMapper<AttachResponse>(objectMapper).data }
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }.onFailure {
        logger.error(it)
    }

    override suspend fun trickleComplete(session: WebSocketSession, request: TrickleCandidateRequest<TrickleCompletedBody>) {
        transactionHelper.sendJanusMessage(session, request)
    }

    override suspend fun trickle(session: WebSocketSession, request: TrickleCandidateRequest<TrickleCandidateBody>) {
        transactionHelper.sendJanusMessage(session, request)
    }
}