package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.client.janus.dto.plugin.TrickleCandidateBody
import com.youhajun.transcall.client.janus.dto.plugin.TrickleCandidateRequest
import com.youhajun.transcall.client.janus.dto.plugin.TrickleCompletedBody
import com.youhajun.transcall.client.janus.dto.video.JSEPType
import com.youhajun.transcall.client.janus.dto.video.VideoRoomJsep
import com.youhajun.transcall.client.janus.dto.video.request.*
import com.youhajun.transcall.client.janus.service.JanusPluginService
import com.youhajun.transcall.client.janus.service.JanusVideoRoomService
import com.youhajun.transcall.ws.dto.*
import com.youhajun.transcall.ws.dto.payload.*
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MediaContentType
import com.youhajun.transcall.ws.vo.MessageType
import com.youhajun.transcall.ws.vo.PublisherInfo
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

@Component
class SignalingMessageHandler(
    private val objectMapper: ObjectMapper,
    private val roomSessionManager: RoomSessionManager,
    private val janusVideoRoomService: JanusVideoRoomService,
    private val callRoomService: CallRoomService,
    private val janusPluginService: JanusPluginService,
) : WebSocketMessageHandler {

    override fun supports(type: MessageType): Boolean = type == MessageType.SIGNALING

    override suspend fun handle(userId: UUID, roomId: UUID, session: WebSocketSession, msg: ClientMessage) {
        when (val payload = msg.payload as SignalingRequest) {
            is JoinRoomPublisher -> payload.joinPublish(roomId, userId)
            is PublisherOffer -> payload.publish(roomId, userId)
            is SubscriberAnswer -> payload.start(roomId, userId)
            is SignalingIceCandidate -> payload.iceCandidate(roomId, userId)
            is CompleteIceCandidate -> payload.completeIceCandidate(roomId, userId)
            is JoinRoomSubscriber -> payload.joinSubscriber(roomId, userId)
            is SubscriberUpdate -> payload.updateSubscriber(roomId, userId)
        }
    }

    private suspend fun JoinRoomPublisher.joinPublish(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()
        val janusRoomId = callRoomService.getJanusRoomId(roomId)

        val request = JanusVideoRoomRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            body = JoinPublisherRequestBody(
                janusRoomId = janusRoomId,
                display = userId.toString(),
            ),
        )

        janusVideoRoomService.joinPublish(janusSessionInfo.janusSession, request).onSuccess {
            val publisherInfo = PublisherInfo(
                mediaContentType = mediaContentType,
                feedId = it.feedId,
                privateId = it.privateId
            )
            updatePublisherInfo(roomId, userId, handleId, publisherInfo)
            val feeds = it.publishers.map { feed -> feed.toPublisherFeedResponse() }
            sendJoinedRoomPublisher(participant.userSession, handleId, mediaContentType, feeds, it.privateId)
        }
    }

    private suspend fun PublisherOffer.publish(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        val jsep = VideoRoomJsep(type = JSEPType.OFFER, sdp = offerSdp)
        val body = VideoRoomPublishRequestBody(
            audioCodec = audioCodec?.substringAfter("/"),
            videoCodec = videoCodec?.substringAfter("/"),
            descriptions = buildList {
                audioMid?.let { add(JanusStreamDescription(it, mediaContentType.type)) }
                videoMid?.let { add(JanusStreamDescription(it, mediaContentType.type)) }
            }
        )
        val request = JanusVideoRoomRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            body = body,
            jsep = jsep,
        )

        janusVideoRoomService.publish(janusSessionInfo.janusSession, request).onSuccess {
            val answerSdp = it.jsep?.sdp ?: throw WebSocketException.PublisherAnswerIsNull()
            sendPublisherAnswer(participant.userSession, handleId, answerSdp)
        }
    }

    private suspend fun JoinRoomSubscriber.joinSubscriber(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()
        val handleId = janusSessionInfo.videoRoomHandleInfo.subscriberHandleId
        if(feeds.isEmpty()) return

        val janusRoomId = callRoomService.getJanusRoomId(roomId)

        val request = JanusVideoRoomRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            body = JoinSubscriberRequestBody(
                janusRoomId = janusRoomId,
                privateId = privateId,
                streams = feeds.map { it.toSubscribeStreamRequest() }
            )
        )

        janusVideoRoomService.joinSubscribe(janusSessionInfo.janusSession, request).onSuccess { res ->
            val feeds = res.pluginData.data.streams.map { it.toSubscriberFeedResponse() }
            val sdpOffer = res.jsep?.sdp ?: throw WebSocketException.SubscriberOfferIsNull()
            sendSubscriberOffer(participant.userSession, handleId, sdpOffer, feeds)
        }
    }

    private suspend fun SubscriberUpdate.updateSubscriber(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        val handleId = janusSessionInfo.videoRoomHandleInfo.subscriberHandleId

        if (subscribeFeeds.isEmpty() && unsubscribeFeeds.isEmpty()) return

        val request = JanusVideoRoomRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            body = VideoRoomSubscriberUpdateRequestBody(
                subscribe = subscribeFeeds.map { it.toSubscribeStreamRequest() },
                unsubscribe = unsubscribeFeeds.map { it.toSubscribeStreamRequest() }
            )
        )

        janusVideoRoomService.subscriberUpdate(janusSessionInfo.janusSession, request)
    }

    private suspend fun SubscriberAnswer.start(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        val jsep = VideoRoomJsep(type = JSEPType.ANSWER, sdp = answerSdp)
        val body = VideoRoomStartRequestBody()
        val request = JanusVideoRoomRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            body = body,
            jsep = jsep,
        )
        janusVideoRoomService.start(janusSessionInfo.janusSession, request)
    }

    private suspend fun SignalingIceCandidate.iceCandidate(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        val candidate = TrickleCandidateBody(
            candidate = candidate,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex
        )
        val request = TrickleCandidateRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            candidate = candidate
        )
        janusPluginService.trickle(janusSessionInfo.janusSession, request)
    }

    private suspend fun CompleteIceCandidate.completeIceCandidate(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val janusSessionInfo = participant.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        val candidate = TrickleCompletedBody()
        val request = TrickleCandidateRequest(
            sessionId = janusSessionInfo.janusSessionId,
            handleId = handleId,
            candidate = candidate
        )
        janusPluginService.trickleComplete(janusSessionInfo.janusSession, request)
    }

    private suspend fun sendPublisherAnswer(userSession: WebSocketSession, publisherHandleId: Long, answerSdp: String) {
        val payload = PublisherAnswer(publisherHandleId = publisherHandleId, answerSdp = answerSdp)
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun sendSubscriberOffer(userSession: WebSocketSession, handleId: Long, offerSdp: String, feeds: List<SubscriberFeedResponse>) {
        val payload = SubscriberOffer(offerSdp = offerSdp, subscriberHandleId = handleId, feeds = feeds)
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun sendJoinedRoomPublisher(
        userSession: WebSocketSession,
        publisherHandleId: Long,
        mediaContentType: MediaContentType,
        feeds: List<PublisherFeedResponse>,
        privateId: Long? = null,
    ) {
        val payload = JoinedRoomPublisher(
            publisherHandleId = publisherHandleId,
            mediaContentType = mediaContentType,
            feeds = feeds,
            privateId = privateId
        )
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun updatePublisherInfo(roomId: UUID, userId: UUID, handleId: Long, publisherInfo: PublisherInfo) {
        roomSessionManager.updateUserSession(roomId, userId) {
            it.copy(janusSessionInfo = it.janusSessionInfo?.copy(
                publisherInfoMap = it.janusSessionInfo.publisherInfoMap + (handleId to publisherInfo)
            ))
        }
    }
}