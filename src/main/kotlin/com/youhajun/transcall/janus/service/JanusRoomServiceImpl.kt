package com.youhajun.transcall.janus.service

import com.youhajun.transcall.janus.VideoRoomRequestBuilder
import com.youhajun.transcall.janus.dto.*
import com.youhajun.transcall.janus.exception.JanusException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class JanusRoomServiceImpl(
    @Qualifier("janusWebClient") private val httpClient: WebClient,
) : JanusRoomService {
    override suspend fun createRoom(sessionId: Long, handleId: Long): Result<Long> = runCatching {
        val request = VideoRoomRequestBuilder()
            .createRoom()
            .session(sessionId)
            .handle(handleId)
            .build()

        val response = httpClient.post()
            .uri("/$sessionId/$handleId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JanusResponse::class.java)
            .awaitSingle()

        val event = response.requireData<VideoRoomData.Created>()
        event.room ?: throw JanusException.JanusResponseMappingException()
    }
}