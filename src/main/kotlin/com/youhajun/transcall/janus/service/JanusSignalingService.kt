package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.dto.*
import com.youhajun.transcall.ws.vo.JanusSessionInfo
import com.youhajun.transcall.ws.vo.MediaContentType
import org.springframework.stereotype.Service

@Service
interface JanusSignalingService {
    suspend fun joinAsPublisher(
        session: JanusSessionInfo,
        janusRoomId: Long,
        mediaContentType: MediaContentType
    ): Result<JanusJoinPublisherResponse>

    suspend fun publisherOffer(
        session: JanusSessionInfo,
        mediaContentType: MediaContentType,
        offerSdp: String
    ): Result<JanusPublisherAnswerResponse>

    suspend fun joinAsSubscriber(
        session: JanusSessionInfo,
        janusRoomId: Long,
        privateId: Long?,
        feeds: List<SubscriberStream>
    ): Result<JanusSubscriberOfferResponse>

    suspend fun subscriberAnswer(
        session: JanusSessionInfo,
        answerSdp: String
    ): Result<Unit>

    suspend fun updateSubscription(
        session: JanusSessionInfo,
        subscribe: List<SubscriberStream>? = null,
        unsubscribe: List<SubscriberStream>? = null
    ): Result<Unit>

    suspend fun trickleCandidate(
        session: JanusSessionInfo,
        mediaContentType: MediaContentType,
        candidate: JanusIceCandidate,
        isPublisher: Boolean
    ): Result<Unit>

    suspend fun trickleComplete(
        session: JanusSessionInfo,
        mediaContentType: MediaContentType,
        isPublisher: Boolean
    ): Result<Unit>

    suspend fun unpublish(session: JanusSessionInfo, mediaContentType: MediaContentType): Result<Unit>

    suspend fun leave(session: JanusSessionInfo): Result<Unit>
}