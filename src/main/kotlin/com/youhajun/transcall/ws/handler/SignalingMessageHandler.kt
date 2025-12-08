package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.janus.service.JanusSignalingService
import com.youhajun.transcall.ws.dto.ClientMessage
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.*
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MessageType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

@Component
class SignalingMessageHandler(
    private val janusSignalingService: JanusSignalingService,
    private val objectMapper: ObjectMapper,
    private val roomSessionManager: RoomSessionManager,
    private val roomService: CallRoomService
) : WebSocketMessageHandler {

    override fun supports(type: MessageType): Boolean = type == MessageType.SIGNALING

    override suspend fun handle(userId: UUID, roomId: UUID, msg: ClientMessage) {
        when (val payload = msg.payload as SignalingRequest) {
            is JoinPublisher -> joinAsPublisher(roomId, userId, payload)
            is JoinSubscriber -> joinAsSubscriber(roomId, userId, payload)
            is PublisherOffer -> publisherOffer(roomId, userId, payload)
            is SubscriberAnswer -> subscriberAnswer(roomId, userId, payload)
            is SubscriberUpdate -> updateSubscription(roomId, userId, payload)
            is SignalingIceCandidate -> trickleCandidate(roomId, userId, payload)
            is IceCandidateComplete -> trickleComplete(roomId, userId, payload)
        }
    }

    private suspend fun joinAsPublisher(roomId: UUID, userId: UUID, payload: JoinPublisher) {
        val session = roomSessionManager.requireContext(roomId, userId)
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)
        val janusRoomId = roomService.getJanusRoomId(roomId)

        janusSignalingService.joinAsPublisher(janusSession, janusRoomId, payload.mediaContentType).onSuccess {
            val message = JoinedEvent(
                myFeedId = it.myFeedId,
                privateId = it.myPrivateId,
                publishers = it.publishers,
                mediaContentType = payload.mediaContentType
            )
            sendToClient(session.userSession, message)
        }
    }

    private suspend fun joinAsSubscriber(roomId: UUID, userId: UUID, payload: JoinSubscriber) {
        val session = roomSessionManager.requireContext(roomId, userId)
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)
        val janusRoomId = roomService.getJanusRoomId(roomId)

        janusSignalingService.joinAsSubscriber(janusSession, janusRoomId, payload.privateId, payload.subscribeFeeds).onSuccess {
            val message = SubscriberOffer(
                offerSdp = it.offerSdp,
                feeds = it.feeds
            )

            sendToClient(session.userSession, message)
        }
    }

    private suspend fun publisherOffer(roomId: UUID, userId: UUID, payload: PublisherOffer) {
        val session = roomSessionManager.requireContext(roomId, userId)
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)

        janusSignalingService.publisherOffer(janusSession, payload.mediaContentType, payload.offerSdp).onSuccess {
            val message = PublisherAnswer(answerSdp = it.answerSdp, mediaContentType = payload.mediaContentType)
            sendToClient(session.userSession, message)
        }
    }

    private suspend fun subscriberAnswer(roomId: UUID, userId: UUID, payload: SubscriberAnswer) {
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)

        janusSignalingService.subscriberAnswer(janusSession, payload.answerSdp)
    }

    private suspend fun updateSubscription(roomId: UUID, userId: UUID, payload: SubscriberUpdate) {
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)

        janusSignalingService.updateSubscription(janusSession, payload.subscribeFeeds, payload.unsubscribeFeeds)
    }

    private suspend fun trickleCandidate(roomId: UUID, userId: UUID, payload: SignalingIceCandidate) {
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)

        janusSignalingService.trickleCandidate(janusSession, payload.mediaContentType, payload.janusCandidate, payload.isPublisher)
    }

    private suspend fun trickleComplete(roomId: UUID, userId: UUID, payload: IceCandidateComplete) {
        val janusSession = roomSessionManager.requireJanusSession(roomId, userId)

        janusSignalingService.trickleComplete(janusSession, payload.mediaContentType, payload.isPublisher)
    }

    private suspend fun sendToClient(session: WebSocketSession, payload: SignalingResponse) {
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        session.sendServerMessage(message, objectMapper)
    }
}