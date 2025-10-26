package com.youhajun.transcall.janus.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.event.JanusEvent
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

        val event = runCatching { objectMapper.readValue(payloadText, JanusEvent::class.java) }.onFailure {
            logger.warn("Failed to parse message: $it")
        }.getOrNull()

        event?.let { _eventMessage.emit(it) }
    }
}
