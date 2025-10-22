package com.youhajun.transcall.client.openai.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.client.openai.dto.OpenAiEvent
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

class OpenAiWebsocketHandler(
    private val objectMapper: ObjectMapper,
    private val connectionReady: CompletableDeferred<OpenAiConnection>
) : WebSocketHandler {

    private val logger: Logger = LogManager.getLogger(OpenAiWebsocketHandler::class.java)

    private val _eventMessage = MutableSharedFlow<OpenAiEvent>()

    override fun handle(session: WebSocketSession): Mono<Void> = mono {
        logger.info("Connecting to OpenAi WebSocket at {}", session.handshakeInfo.uri)
        val connection = OpenAiConnection(session = session, eventFlow = _eventMessage.asSharedFlow())
        connectionReady.complete(connection)

        session.receive()
            .doOnError { logger.error("Connection Error ${it.message}") }
            .map { it.payloadAsText }
            .asFlow()
            .collect(::handleOpenAiMessage)

        return@mono null
    }

    private suspend fun handleOpenAiMessage(payloadText: String) {

    }
}
