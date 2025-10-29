package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.dto.plugin.*
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.util.JanusTransactionHelper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.WebSocketSession

@Service
class JanusPluginServiceImpl(
    private val transactionHelper: JanusTransactionHelper,
    @Qualifier("janusWebClient") private val client: WebClient,
) : JanusPluginService {

    private val logger: Logger = LogManager.getLogger(JanusPluginServiceImpl::class.java)

    override suspend fun attachPlugin(
        session: WebSocketSession,
        sessionId: Long,
        plugin: JanusPlugin
    ): Result<AttachData> {
        val request = AttachRequest(plugin = plugin.pkgName, sessionId = sessionId)
        return transactionHelper
            .requestJanusResponse<AttachResponse>(session, request)
            .map { it.data }
    }

    override suspend fun attachManagerPlugin(
        sessionId: Long,
        plugin: JanusPlugin
    ): Result<AttachData> = runCatching {
        val request = AttachRequest(plugin = plugin.pkgName, sessionId = sessionId)
        client.post()
            .uri("/janus")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<AttachResponse>() {})
            .map { it.data }
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }.onFailure { logger.error(it) }

    override suspend fun trickle(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    ) {
        val body = TrickleCandidateBody(
            candidate = candidate,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex
        )
        val req = TrickleCandidateRequest(
            sessionId = sessionId,
            handleId = handleId,
            candidate = body
        )
        transactionHelper.sendJanusMessage(session, req)
    }

    override suspend fun trickleComplete(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long
    ) {
        val body = TrickleCompletedBody()
        val req = TrickleCandidateRequest(
            sessionId = sessionId,
            handleId = handleId,
            candidate = body
        )
        transactionHelper.sendJanusMessage(session, req)
    }
}