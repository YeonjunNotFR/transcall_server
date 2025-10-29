package com.youhajun.transcall.janus.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.plugin.JanusPlugin
import com.youhajun.transcall.janus.dto.plugin.JanusPluginResponse
import com.youhajun.transcall.janus.dto.video.JSEPType
import com.youhajun.transcall.janus.dto.video.VideoRoomJsep
import com.youhajun.transcall.janus.dto.video.request.*
import com.youhajun.transcall.janus.dto.video.response.*
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.util.JanusControlManager
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
class JanusVideoRoomServiceImpl(
    @Qualifier("janusWebClient") private val client: WebClient,
    private val transactionHelper: JanusTransactionHelper,
    private val janusControlManager: JanusControlManager
) : JanusVideoRoomService {

    private val logger: Logger = LogManager.getLogger(JanusVideoRoomServiceImpl::class.java)

    override suspend fun createRoom(janusRoomId: Long): Result<CreateRoomResponse> = runCatching {
        val sessionId = janusControlManager.getSessionId()
        val handleId = janusControlManager.getHandleId(JanusPlugin.VIDEO_ROOM)
        val request = CreateRoomBody(janusRoomId = janusRoomId)
        val janusRequest = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = request)

        client.post()
            .uri("/janus/$sessionId/$handleId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(janusRequest)
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<CreateRoomResponse>() {})
            .awaitSingleOrNull() ?: throw JanusException.JanusResponseMappingException()
    }.onFailure {
        logger.error(it)
        if (it is JanusException.JanusResponseException && (it.code == 460 || it.code == 458)) {
            janusControlManager.refreshSession()
        }
    }

    override suspend fun joinPublish(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        janusRoomId: Long,
        display: String,
    ): Result<JoinPublisherResponse> {
        val body = JoinPublisherBody(janusRoomId = janusRoomId, display = display)
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = body)
        return transactionHelper
            .requestJanusResponse<JanusPluginResponse<JoinPublisherResponse>>(session, req)
            .map { it.pluginData.data }
    }

    override suspend fun unpublish(session: WebSocketSession, sessionId: Long, handleId: Long) {
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = UnpublishBody)
        transactionHelper.sendJanusMessage(session, req)
    }

    override suspend fun leave(session: WebSocketSession, sessionId: Long, handleId: Long) {
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = LeaveBody)
        transactionHelper.sendJanusMessage(session, req)
    }

    override suspend fun joinSubscribe(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        janusRoomId: Long,
        privateId: Long?,
        streams: List<SubscribeStreamBody>,
    ): Result<JanusPluginResponse<VideoRoomSubscribeResponse>> {
        val body = JoinSubscriberBody(janusRoomId = janusRoomId, privateId = privateId, streams = streams)
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = body)
        return transactionHelper.requestJanusResponse(session, req)
    }

    override suspend fun publish(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        offerSdp: String,
        audioCodec: String?,
        videoCodec: String?,
        descriptions: List<StreamDescription>,
    ): Result<JanusPluginResponse<VideoRoomEventResponse>> {
        val jsep = VideoRoomJsep(type = JSEPType.OFFER, sdp = offerSdp)
        val body = PublishBody(
            audioCodec = audioCodec,
            videoCodec = videoCodec,
            descriptions = descriptions
        )
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = body, jsep = jsep)
        return transactionHelper.requestJanusResponse(session, req)
    }

    override suspend fun subscriberUpdate(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        subscribe: List<SubscribeStreamBody>?,
        unsubscribe: List<SubscribeStreamBody>?,
    ): Result<JanusPluginResponse<VideoRoomSubscribeResponse>> {
        val body = UpdateBody(
            subscribe = subscribe.takeIf { !it.isNullOrEmpty() },
            unsubscribe = unsubscribe.takeIf { !it.isNullOrEmpty() },
        )
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = body)
        return transactionHelper.requestJanusResponse(session, req)
    }

    override suspend fun start(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        answerSdp: String,
    ): Result<VideoRoomEventResponse> {
        val jsep = VideoRoomJsep(type = JSEPType.ANSWER, sdp = answerSdp)
        val req = JanusVideoRoomRequest(sessionId = sessionId, handleId = handleId, body = StartBody, jsep = jsep)
        return transactionHelper
            .requestJanusResponse<JanusPluginResponse<VideoRoomEventResponse>>(session, req)
            .map { it.pluginData.data }
    }
}
