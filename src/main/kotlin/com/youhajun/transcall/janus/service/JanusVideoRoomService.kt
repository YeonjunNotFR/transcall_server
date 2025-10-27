package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.dto.plugin.JanusPluginResponse
import com.youhajun.transcall.janus.dto.video.request.*
import com.youhajun.transcall.janus.dto.video.response.*
import org.springframework.web.reactive.socket.WebSocketSession

interface JanusVideoRoomService {

    suspend fun createRoom(
        janusRoomId: Long
    ): Result<CreateVideoRoomResponse>

    suspend fun joinPublish(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        janusRoomId: Long,
        display: String,
    ): Result<JoinPublisherResponse>

    suspend fun unpublish(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
    )

    suspend fun leave(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
    )

    suspend fun joinSubscribe(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        janusRoomId: Long,
        privateId: Long?,
        streams: List<SubscribeStreamBody>,
    ): Result<JanusPluginResponse<JoinSubscriberResponse>>

    suspend fun publish(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        offerSdp: String,
        audioCodec: String?,
        videoCodec: String?,
        descriptions: List<StreamDescription>,
    ): Result<JanusPluginResponse<VideoRoomPublishResponse>>

    suspend fun subscriberUpdate(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        subscribe: List<SubscribeStreamBody>?,
        unsubscribe: List<SubscribeStreamBody>?,
    ): Result<JanusPluginResponse<VideoRoomSubscribeResponse>>

    suspend fun start(
        session: WebSocketSession,
        sessionId: Long,
        handleId: Long,
        answerSdp: String,
    ): Result<VideoRoomStartResponse>
}