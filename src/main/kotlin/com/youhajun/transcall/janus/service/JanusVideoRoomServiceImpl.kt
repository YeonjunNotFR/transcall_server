package com.youhajun.transcall.janus.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.plugin.JanusPlugin
import com.youhajun.transcall.janus.dto.plugin.JanusPluginResponse
import com.youhajun.transcall.janus.dto.video.request.*
import com.youhajun.transcall.janus.dto.video.response.*
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.util.JanusControlManager
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
class JanusVideoRoomServiceImpl(
    @Qualifier("janusWebClient") private val client: WebClient,
    private val objectMapper: ObjectMapper,
    private val transactionHelper: JanusTransactionHelper,
    private val janusControlManager: JanusControlManager
) : JanusVideoRoomService {

    private val logger: Logger = LogManager.getLogger(JanusVideoRoomServiceImpl::class.java)

    override suspend fun createRoom(request: CreateVideoRoomRequest): Result<CreateVideoRoomResponse> = runCatching {
        val sessionId = janusControlManager.getSessionId()
        val handleId = janusControlManager.getHandleId(JanusPlugin.VIDEO_ROOM)
        val janusRequest = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = request)

        client.post()
            .uri("/janus/$sessionId/$handleId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(janusRequest)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .map { it.janusResponseMapper<CreateVideoRoomResponse>(objectMapper) }
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }.onFailure {
        logger.error(it)
        if(it is JanusException.JanusResponseException && (it.code == 460 || it.code == 458)) {
            janusControlManager.refreshSession()
        }
    }

    override suspend fun joinPublish(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<JoinPublisherRequestBody>
    ): Result<JoinPublisherResponse> =
        transactionHelper.requestJanusResponse<JanusPluginResponse<JoinPublisherResponse>>(session, request)
            .map { it.pluginData.data }

    override suspend fun joinSubscribe(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<JoinSubscriberRequestBody>
    ): Result<JanusPluginResponse<JoinSubscriberResponse>> =
        transactionHelper.requestJanusResponse<JanusPluginResponse<JoinSubscriberResponse>>(session, request)

    override suspend fun publish(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<VideoRoomPublishRequestBody>
    ): Result<JanusPluginResponse<VideoRoomPublishResponse>> =
        transactionHelper.requestJanusResponse<JanusPluginResponse<VideoRoomPublishResponse>>(session, request)

    override suspend fun subscriberUpdate(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<VideoRoomSubscriberUpdateRequestBody>
    ): Result<VideoRoomSubscribeResponse> =
        transactionHelper.requestJanusResponse<JanusPluginResponse<VideoRoomSubscribeResponse>>(session, request)
            .map { it.pluginData.data }

    override suspend fun start(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<VideoRoomStartRequestBody>
    ): Result<VideoRoomStartResponse> =
        transactionHelper.requestJanusResponse<JanusPluginResponse<VideoRoomStartResponse>>(session, request)
            .map { it.pluginData.data }
}
