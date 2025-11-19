package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.JanusWebSocketClient
import com.youhajun.transcall.janus.VideoRoomRequestBuilder
import com.youhajun.transcall.janus.dto.*
import com.youhajun.transcall.ws.vo.JanusSessionInfo
import com.youhajun.transcall.ws.vo.MediaContentType
import org.springframework.stereotype.Service

@Service
class JanusSignalingServiceImpl(
    private val client: JanusWebSocketClient,
) : JanusSignalingService {

    override suspend fun joinAsPublisher(
        session: JanusSessionInfo,
        janusRoomId: Long,
        mediaContentType: MediaContentType
    ): Result<JanusJoinPublisherResponse> = runCatching {
        val request = VideoRoomRequestBuilder()
            .joinPublisher(room = janusRoomId)
            .session(session.sessionId)
            .handle(session.handleInfo.getPubHandleId(mediaContentType))
            .build()

        val response = client.sendRequest(session.session, request)
        val joined = response.requireData<VideoRoomData.Joined>()

        JanusJoinPublisherResponse(
            myFeedId = joined.id,
            myPrivateId = joined.privateId ?: 0L,
            publishers = joined.publishers ?: emptyList()
        )
    }

    override suspend fun publisherOffer(
        session: JanusSessionInfo,
        mediaContentType: MediaContentType,
        offerSdp: String
    ): Result<JanusPublisherAnswerResponse> = runCatching {
        val request = VideoRoomRequestBuilder()
            .configure(audio = true, video = true)
            .session(session.sessionId)
            .handle(session.handleInfo.getPubHandleId(mediaContentType))
            .jsep(Jsep(type = JSEPType.OFFER, sdp = offerSdp))
            .build()

        val response = client.sendRequest(session.session, request)
        val answerSdp = response.requireJsep()

        JanusPublisherAnswerResponse(answerSdp = answerSdp.sdp)
    }

    override suspend fun joinAsSubscriber(
        session: JanusSessionInfo,
        janusRoomId: Long,
        privateId: Long?,
        feeds: List<SubscriberStream>
    ): Result<JanusSubscriberOfferResponse> = runCatching {
        val request = VideoRoomRequestBuilder()
            .joinSubscriber(room = janusRoomId, streams = feeds, privateId = privateId)
            .session(session.sessionId)
            .handle(session.handleInfo.subscriberHandleId)
            .build()

        val response = client.sendRequest(session.session, request)
        val offerSdp = response.requireJsep()
        val attached = response.requireData<VideoRoomData.Attached>()
        val mappings = attached.streams?.map { stream ->
            val originalRequest = feeds.find { it.mid == stream.mid }
            stream.copy(feedId = originalRequest?.feed)
        } ?: emptyList()

        JanusSubscriberOfferResponse(
            offerSdp = offerSdp.sdp,
            feeds = mappings
        )
    }

    override suspend fun subscriberAnswer(
        session: JanusSessionInfo,
        answerSdp: String
    ): Result<Unit> = runCatching {
        val request = VideoRoomRequestBuilder()
            .start()
            .session(session.sessionId)
            .handle(session.handleInfo.subscriberHandleId)
            .jsep(Jsep(type = JSEPType.ANSWER, sdp = answerSdp))
            .build()

        client.sendRequest(session.session, request)
        Unit
    }

    override suspend fun updateSubscription(
        session: JanusSessionInfo,
        subscribe: List<SubscriberStream>?,
        unsubscribe: List<SubscriberStream>?
    ): Result<Unit> = runCatching {
        val request = VideoRoomRequestBuilder()
            .update(subscribe = subscribe, unsubscribe = unsubscribe)
            .session(session.sessionId)
            .handle(session.handleInfo.subscriberHandleId)
            .build()

        client.sendRequest(session.session, request)
        Unit
    }

    override suspend fun trickleCandidate(
        session: JanusSessionInfo,
        mediaContentType: MediaContentType,
        candidate: JanusIceCandidate,
        isPublisher: Boolean
    ): Result<Unit> = runCatching {
        val handleId = if (isPublisher) {
            session.handleInfo.getPubHandleId(mediaContentType)
        } else {
            session.handleInfo.subscriberHandleId
        }

        val request = VideoRoomRequestBuilder()
            .trickle(candidate)
            .session(session.sessionId)
            .handle(handleId)
            .build()

        client.sendRequest(session.session, request)
        Unit
    }

    override suspend fun trickleComplete(
        session: JanusSessionInfo,
        mediaContentType: MediaContentType,
        isPublisher: Boolean
    ): Result<Unit> = runCatching {
        val handleId = if (isPublisher) {
            session.handleInfo.getPubHandleId(mediaContentType)
        } else {
            session.handleInfo.subscriberHandleId
        }

        val request = VideoRoomRequestBuilder()
            .trickleComplete()
            .session(session.sessionId)
            .handle(handleId)
            .build()

        client.sendRequest(session.session, request)
        Unit
    }

    override suspend fun leave(session: JanusSessionInfo): Result<Unit> = runCatching {
        val request = VideoRoomRequestBuilder()
            .leave()
            .session(session.sessionId)
            .handle(session.handleInfo.defaultPublisherHandleId)
            .build()

        client.sendRequest(session.session, request)
        Unit
    }

    override suspend fun unpublish(session: JanusSessionInfo, mediaContentType: MediaContentType): Result<Unit> = runCatching {
        val request = VideoRoomRequestBuilder()
            .unpublish()
            .session(session.sessionId)
            .handle(session.handleInfo.getPubHandleId(mediaContentType))
            .build()

        client.sendRequest(session.session, request)
        Unit
    }
}