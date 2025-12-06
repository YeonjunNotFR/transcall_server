package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.auth.jwt.JwtProvider
import com.youhajun.transcall.ws.dto.ClientMessage
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.getParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.mono
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.*

@Component
class RoomWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val messageHandlers: List<WebSocketMessageHandler>,
    private val jwtProvider: JwtProvider,
    private val janusHandler: JanusHandler,
    private val whisperHandler: WhisperHandler,
    private val roomFacade: RoomFacade
) : WebSocketHandler {

    companion object {
        private const val ROOM_ID_QUERY_PARAM = "roomId"
        private const val TOKEN_QUERY_PARAM = "token"
    }

    private val logger: Logger = LogManager.getLogger(RoomWebSocketHandler::class.java)

    override fun handle(session: WebSocketSession): Mono<Void> {
        val params = session.getParams()
        val roomId = params.getFirst(ROOM_ID_QUERY_PARAM)?.let(UUID::fromString) ?: return Mono.error(WebSocketException.MissingRoomId())
        val token = params.getFirst(TOKEN_QUERY_PARAM) ?: return Mono.error(WebSocketException.MissingToken())
        val userId = validateToken(token).let(UUID::fromString) ?: return Mono.error(WebSocketException.InvalidToken())

        return mono {
            roomFacade.prepareEntry(roomId, userId, session)
            coroutineScope {
                launch { janusHandler.setupJanus(roomId, userId) }
                //              launch { whisperHandler.connectWhisper(roomId, userId) }
            }
            roomFacade.completeEntry(roomId, userId)
        }.then(
            session.receive()
                .concatMap { message -> handleMessage(roomId, userId, message) }
                .then()
        ).doFinally {
            logger.info("WebSocket disconnected: userId=$userId")
            CoroutineScope(Dispatchers.IO).launch {
                cleanup(roomId, userId)
            }
        }
    }

    private fun validateToken(token: String): String = runCatching {
        val claims = jwtProvider.parseAccessToken(token)
        return claims.subject
    }.getOrElse { throw WebSocketException.InvalidToken() }

    private fun handleMessage(roomId: UUID, userId: UUID, message: WebSocketMessage): Mono<Void> {
        val payload = extractPayload(message) ?: return Mono.empty()
        return mono {
            when (payload) {
                is String -> handleTextMessage(roomId, userId, payload)
                is ByteArray -> handleBinaryMessage(roomId, userId, payload)
                else -> Unit
            }
        }.then().onErrorResume { error ->
            logger.warn("Failed to process message for userId=$userId", error)
            Mono.empty()
        }
    }

    private fun extractPayload(message: WebSocketMessage): Any? {
        return when (message.type) {
            WebSocketMessage.Type.TEXT -> message.payloadAsText
            WebSocketMessage.Type.BINARY -> message.payload.asInputStream().use { it.readAllBytes() }
            else -> null
        }
    }

    private suspend fun handleTextMessage(roomId: UUID, userId: UUID, payload: String) {
        val clientMsg = objectMapper.readValue(payload, ClientMessage::class.java)
        messageHandlers
            .firstOrNull { it.supports(clientMsg.type) }
            ?.handle(userId, roomId, clientMsg)
    }

    private suspend fun handleBinaryMessage(roomId: UUID, userId: UUID, bytes: ByteArray) {
//        whisperHandler.sendAudioData(roomId, userId, bytes)
    }

    private suspend fun cleanup(roomId: UUID, userId: UUID) {
        runCatching {
            janusHandler.disposeJanus(roomId, userId)
            // whisperHandler.disposeWhisper(roomId, userId)
            roomFacade.processLeave(roomId, userId)
        }.onFailure { error ->
            logger.error("Cleanup failed: userId=$userId, roomId=$roomId", error)
        }
    }
}