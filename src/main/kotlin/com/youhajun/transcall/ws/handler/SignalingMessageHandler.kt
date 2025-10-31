package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.janus.dto.video.request.StreamDescription
import com.youhajun.transcall.janus.service.JanusPluginService
import com.youhajun.transcall.janus.service.JanusVideoRoomService
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

    private fun context(roomId: UUID, userId: UUID) = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()

    override suspend fun handle(userId: UUID, roomId: UUID, session: WebSocketSession, msg: ClientMessage) {
        when (val payload = msg.payload as SignalingRequest) {
            is JoinRoomPublisher -> joinPublish(roomId, userId, payload)
            is PublisherOffer -> publish(roomId, userId, payload)
            is JoinRoomSubscriber -> joinSubscriber(roomId, userId, payload)
            is SubscriberUpdate -> updateSubscriber(roomId, userId, payload)
            is SubscriberAnswer -> start(roomId, userId, payload)
            is SignalingIceCandidate -> iceCandidate(roomId, userId, payload)
            is CompleteIceCandidate -> completeIceCandidate(roomId, userId, payload)
        }
    }

    private suspend fun joinPublish(roomId: UUID, userId: UUID, payload: JoinRoomPublisher) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()
        val janusRoomId = callRoomService.getJanusRoomId(roomId)
        janusVideoRoomService.joinPublish(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = payload.handleId,
            janusRoomId = janusRoomId,
            display = userId.toString()
        ).onSuccess {
            val feeds = it.publishers.map { it.toPublisherFeedResponse() }
            val publisherInfo = PublisherInfo(
                mediaContentType = payload.mediaContentType,
                feedId = it.feedId,
                privateId = it.privateId
            )
            updatePublisherInfo(roomId, userId, payload.handleId, publisherInfo)
            sendJoinedRoomPublisher(context.userSession, payload.handleId, payload.mediaContentType, feeds, it.privateId)
        }
    }

    private suspend fun publish(roomId: UUID, userId: UUID, payload: PublisherOffer) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()
        janusVideoRoomService.publish(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = payload.handleId,
            offerSdp = payload.offerSdp,
            audioCodec = payload.audioCodec?.substringAfter("/"),
            videoCodec = payload.videoCodec?.substringAfter("/"),
            descriptions = buildList {
                payload.audioMid?.let { add(StreamDescription(it, payload.mediaContentType.type)) }
                payload.videoMid?.let { add(StreamDescription(it, payload.mediaContentType.type)) }
            }
        ).onSuccess {
            val answerSdp = it.jsep?.sdp ?: throw WebSocketException.PublisherAnswerIsNull()
            sendPublisherAnswer(context.userSession, payload.handleId, answerSdp)
        }
    }

    private suspend fun joinSubscriber(roomId: UUID, userId: UUID, payload: JoinRoomSubscriber) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()
        val subscriberHandleId = sessionInfo.videoRoomHandleInfo.subscriberHandleId
        val janusRoomId = callRoomService.getJanusRoomId(roomId)

        val myFeeds = sessionInfo.publisherInfoMap.values.map { it.feedId }
        val filtered = payload.feeds.filter { it.feedId !in myFeeds }
        if (filtered.isEmpty()) return

        janusVideoRoomService.joinSubscribe(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = subscriberHandleId,
            janusRoomId = janusRoomId,
            privateId = payload.privateId,
            streams = filtered.map { it.toSubscribeStreamRequest() }
        ).onSuccess {
            val feeds = it.pluginData.data.streams.map { it.toSubscriberFeedResponse() }
            val sdpOffer = it.jsep?.sdp ?: throw WebSocketException.SubscriberOfferIsNull()
            sendSubscriberOffer(context.userSession, subscriberHandleId, sdpOffer, feeds)
        }
    }

    private suspend fun updateSubscriber(roomId: UUID, userId: UUID, payload: SubscriberUpdate) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()
        val subscriberHandleId = sessionInfo.videoRoomHandleInfo.subscriberHandleId

        val subs = payload.subscribeFeeds
        val unsubs = payload.unsubscribeFeeds
        if (subs.isEmpty() && unsubs.isEmpty()) return

        janusVideoRoomService.subscriberUpdate(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = subscriberHandleId,
            subscribe = subs.takeIf { it.isNotEmpty() }?.map { it.toSubscribeStreamRequest() },
            unsubscribe = unsubs.takeIf { it.isNotEmpty() }?.map { it.toSubscribeStreamRequest() }
        ).onSuccess { res ->
            res.jsep?.sdp?.let { newOffer ->
                val feeds = res.pluginData.data.streams.map { it.toSubscriberFeedResponse() }
                sendSubscriberOffer(context.userSession, subscriberHandleId, newOffer, feeds)
            }
        }
    }

    private suspend fun start(roomId: UUID, userId: UUID, payload: SubscriberAnswer) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        janusVideoRoomService.start(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = payload.handleId,
            answerSdp = payload.answerSdp,
        )
    }

    private suspend fun iceCandidate(roomId: UUID, userId: UUID, payload : SignalingIceCandidate) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        janusPluginService.trickle(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = payload.handleId,
            candidate = payload.candidate,
            sdpMid = payload.sdpMid,
            sdpMLineIndex = payload.sdpMLineIndex
        )
    }

    private suspend fun completeIceCandidate(roomId: UUID, userId: UUID, payload: CompleteIceCandidate) {
        val context = context(roomId, userId)
        val sessionInfo = context.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        janusPluginService.trickleComplete(
            session = sessionInfo.janusSession,
            sessionId = sessionInfo.janusSessionId,
            handleId = payload.handleId
        )
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