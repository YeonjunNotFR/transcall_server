package com.youhajun.transcall.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.ws.dto.ServerMessage
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

private val logger: Logger = LogManager.getLogger("WebSocketExt")

suspend fun WebSocketSession.sendServerMessage(serverMessage: ServerMessage, objectMapper: ObjectMapper) {
    if(!isOpen) { logger.warn("Socket is closed"); return }
    val payload = objectMapper.writeValueAsString(serverMessage)
    send(Mono.just(textMessage(payload))).awaitSingleOrNull()?.also {
        logger.info("Sending Server Message: {}", payload)
    }
}

suspend fun WebSocketSession.sendBinaryMessage(message: ByteArray) {
    if(!isOpen) { logger.warn("Socket is closed"); return }
    val wsMsg = binaryMessage { factory -> factory.wrap(message) }
    send(Mono.just(wsMsg)).awaitFirstOrNull()
}

fun WebSocketSession.getParams(): MultiValueMap<String, String> = UriComponentsBuilder
    .fromUri(handshakeInfo.uri)
    .build()
    .queryParams