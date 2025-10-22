package com.youhajun.transcall.whisper.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.whisper.dto.TranscriptUpdate
import com.youhajun.transcall.whisper.dto.WhisperEvent
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

class WhisperWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val connectionReady: CompletableDeferred<WhisperConnection>
) : WebSocketHandler {

    private val logger: Logger = LogManager.getLogger(WhisperWebSocketHandler::class.java)

    private val _eventMessage = MutableSharedFlow<WhisperEvent>()

    override fun handle(session: WebSocketSession): Mono<Void> = mono {
        logger.info("Connecting to Whisper WebSocket at {}", session.handshakeInfo.uri)
        val connection = WhisperConnection(session = session, eventFlow = _eventMessage.asSharedFlow())
        connectionReady.complete(connection)

        session.receive()
            .map { it.payloadAsText }
            .asFlow()
            .collect(::handleWhisperMessage)

        return@mono null
    }

    private suspend fun handleWhisperMessage(payloadText: String) {
        try {
            val event = objectMapper.readValue(payloadText, TranscriptUpdate::class.java)
            _eventMessage.emit(event)
        } catch (e: Exception) {
            logger.error("Failed to parse Whisper message: ${e.message}", e)
        }
    }
}
