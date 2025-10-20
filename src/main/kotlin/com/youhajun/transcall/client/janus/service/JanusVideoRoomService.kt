package com.youhajun.transcall.client.janus.service

import com.youhajun.transcall.client.janus.dto.plugin.JanusPluginResponse
import com.youhajun.transcall.client.janus.dto.video.request.*
import com.youhajun.transcall.client.janus.dto.video.response.*
import org.springframework.web.reactive.socket.WebSocketSession

interface JanusVideoRoomService {

    suspend fun createRoom(
        request: CreateVideoRoomRequest
    ): Result<CreateVideoRoomResponse>

    suspend fun joinPublish(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<JoinPublisherRequestBody>,
    ): Result<JoinPublisherResponse>

    suspend fun joinSubscribe(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<JoinSubscriberRequestBody>,
    ): Result<JanusPluginResponse<JoinSubscriberResponse>>

    suspend fun publish(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<VideoRoomPublishRequestBody>,
    ): Result<JanusPluginResponse<VideoRoomPublishResponse>>

    suspend fun subscriberUpdate(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<VideoRoomSubscriberUpdateRequestBody>,
    ): Result<VideoRoomSubscribeResponse>

    suspend fun start(
        session: WebSocketSession,
        request: JanusVideoRoomRequest<VideoRoomStartRequestBody>,
    ): Result<VideoRoomStartResponse>
}