package com.youhajun.transcall.janus.ws

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.JanusResponseType
import com.youhajun.transcall.janus.dto.event.*
import com.youhajun.transcall.janus.dto.plugin.*
import com.youhajun.transcall.janus.dto.video.response.VideoRoomResponseType
import com.youhajun.transcall.janus.util.JanusTransactionHelper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

class JanusWebSocketHandler(
    private val transactionHelper: JanusTransactionHelper,
    private val objectMapper: ObjectMapper,
    private val connectionReady: CompletableDeferred<JanusConnection>
) : WebSocketHandler {

    private val logger: Logger = LogManager.getLogger(JanusWebSocketHandler::class.java)

    private val _eventMessage = MutableSharedFlow<JanusEvent>()

    override fun getSubProtocols(): MutableList<String> {
        return mutableListOf("janus-protocol")
    }

    override fun handle(session: WebSocketSession): Mono<Void> = mono {
        logger.info("Connecting to Janus WebSocket at {}", session.handshakeInfo.uri)
        val connection = JanusConnection(session = session, eventFlow = _eventMessage.asSharedFlow())
        connectionReady.complete(connection)

        session.receive()
            .doOnError { logger.error("Connection Error ${it.message}") }
            .map { it.payloadAsText }
            .asFlow()
            .collect(::handleJanusMessage)

        return@mono null
    }

    private suspend fun handleJanusMessage(payloadText: String) {
        logger.info("Received Janus message: {}", payloadText)
        transactionHelper.receiveJanusResponse(payloadText)

        val json = objectMapper.readTree(payloadText)
        if(json.has("transaction")) return

        val janusType = json["janus"]?.asText()
        val event = when(JanusResponseType.from(janusType)) {
            JanusResponseType.MEDIA -> objectMapper.convertValue(json, JanusMedia::class.java)
            JanusResponseType.TRICKLE -> parseTrickleCandidate(json)
            JanusResponseType.EVENT -> parsePluginEvent(json)
            else -> null
        }

        event?.let { _eventMessage.emit(it) }
    }

    private fun parseTrickleCandidate(json: JsonNode): TrickleCandidateResponse<out TrickleBody> {
        val isCompleted = json.path("candidate").path("completed").asBoolean(false)
        val typeRef = if(isCompleted) object : TypeReference<TrickleCandidateResponse<TrickleCompletedBody>>() {}
        else object : TypeReference<TrickleCandidateResponse<TrickleCandidateBody>>() {}

        return objectMapper.readValue(objectMapper.treeAsTokens(json), typeRef)
    }

    private fun parsePluginEvent(json: JsonNode): JanusEvent? {
        val pluginData = json.path("plugindata").path("data")
        val videoRoomType = pluginData.path("videoroom")?.asText()

        return when (VideoRoomResponseType.fromType(videoRoomType)) {
            VideoRoomResponseType.EVENT -> parseVideoRoomEvent(json, pluginData)
            else -> null
        }
    }

    private fun parseVideoRoomEvent(json: JsonNode, pluginData: JsonNode): JanusEvent? {
        return when {
            pluginData.has("publishers") -> parseEvent<OnNewPublisher>(json)
            else -> null
        }
    }

    private inline fun <reified T : VideoRoomEvent> parseEvent(json: JsonNode): JanusPluginEvent<T> {
        val typeRef = object : TypeReference<JanusPluginEvent<T>>() {}
        return objectMapper.readValue(objectMapper.treeAsTokens(json), typeRef)
    }
}
