package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.auth.jwt.JwtProvider
import com.youhajun.transcall.call.history.service.CallHistoryService
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.ws.dto.ClientMessage
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.*
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.sendBinaryMessage
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MessageType
import com.youhajun.transcall.ws.vo.RoomParticipantSession
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.util.*

@Component
class RoomWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val messageHandlers: List<WebSocketMessageHandler>,
    private val roomSessionManager: RoomSessionManager,
    private val jwtProvider: JwtProvider,
    private val janusHandler: JanusHandler,
    private val roomService: CallRoomService,
    private val participantService: CallParticipantService,
    private val historyService: CallHistoryService,
    private val whisperHandler: WhisperHandler
) : WebSocketHandler {

    private val logger: Logger = LogManager.getLogger(RoomWebSocketHandler::class.java)

    companion object {
        private const val ROOM_ID_QUERY_PARAM = "roomId"
        private const val TOKEN_QUERY_PARAM = "token"
    }

    override fun handle(session: WebSocketSession): Mono<Void> = mono {
        val params = UriComponentsBuilder
            .fromUri(session.handshakeInfo.uri)
            .build()
            .queryParams

        val roomId = params.getFirst(ROOM_ID_QUERY_PARAM)?.let(UUID::fromString) ?: throw WebSocketException.MissingRoomId()
        val token = params.getFirst(TOKEN_QUERY_PARAM) ?: throw WebSocketException.MissingToken()
        val userId = validateToken(token).let(UUID::fromString)
        var janusSession: WebSocketSession? = null
        var whisperSession: WebSocketSession? = null

        try {
            val isFull = roomService.isRoomFull(roomId)
            if (isFull) throw WebSocketException.RoomFull()

            initSession(roomId, userId, session)
            janusSession = janusHandler.connectJanus(roomId, userId)
            launch { whisperSession = whisperHandler.connectWhisper(roomId, userId) }
            joinSession(roomId, userId, session)

            session.receive()
                .map {
                    when(it.type) {
                        WebSocketMessage.Type.TEXT -> it.payloadAsText
                        WebSocketMessage.Type.BINARY -> it.payload.asInputStream().use { it.readAllBytes() }
                        else -> Unit
                    }
                }
                .asFlow()
                .collect { data ->
                    when(data) {
                        is String -> handleRoomMessage(userId, roomId, session, data)
                        is ByteArray -> handleBinaryMessage(whisperSession, data)
                    }
                }
        } catch (e: Exception) {
            logger.warn("Unable to connect to room $roomId", e)
            when (e) {
                is WebSocketException -> session.close(e.closeStatus).awaitSingleOrNull()
                is JanusException -> session.close(e.closeStatus).awaitSingleOrNull()
                else -> session.close(CloseStatus.SERVER_ERROR).awaitSingleOrNull()
            }
        } finally {
            removeSession(roomId, userId)
            if(session.isOpen) session.close().awaitSingleOrNull()
            if(janusSession?.isOpen == true) janusSession.close().awaitSingleOrNull()
            if(whisperSession?.isOpen == true) whisperSession?.close()?.awaitSingleOrNull()
            this.cancel()
        }

        return@mono null
    }

    private fun validateToken(token: String): String = runCatching {
        val claims = jwtProvider.parseAccessToken(token)
        return claims.subject
    }.getOrElse { throw WebSocketException.InvalidToken() }

    private suspend fun handleBinaryMessage(whisperSession: WebSocketSession?, data: ByteArray) {
        if(whisperSession == null) {
            logger.warn("Whisper session is not initialized")
            return
        }
        whisperSession.sendBinaryMessage(data)
    }

    private suspend fun handleRoomMessage(userId: UUID, roomId: UUID, session: WebSocketSession, payloadText: String) {
        runCatching {
            val clientMsg = objectMapper.readValue(payloadText, ClientMessage::class.java)
            messageHandlers
                .firstOrNull { it.supports(clientMsg.type) }
                ?.handle(userId, roomId, session, clientMsg)
        }.onFailure {
            logger.warn("Invalid client message: ${it.message}")
        }
    }

    private suspend fun initSession(roomId: UUID, userId: UUID, userSession: WebSocketSession) {
        logger.info("Initializing session for user $userId in room $roomId")
        val historyId = historyService.saveCallHistory(userId, roomId)
        val participantId = participantService.saveParticipant(roomId, userId)
        val participantSession = RoomParticipantSession(
            userId = userId,
            userSession = userSession,
            historyId = historyId,
            participantId = participantId
        )
        roomSessionManager.addUserSession(roomId, participantSession)
    }

    private suspend fun joinSession(roomId: UUID, userId: UUID, userSession: WebSocketSession) {
        roomService.updateRoomStatus(roomId)
        roomService.updateCurrentParticipantCount(roomId)
        sendConnected(userId, roomId, userSession)
        broadcastRoomChange(roomId, userId)
    }

    private suspend fun removeSession(roomId: UUID, userId: UUID) {
        logger.info("Removing session for user $userId in room $roomId")
        val participant = roomSessionManager.getUserSession(roomId, userId)
        janusHandler.disposeJanus(roomId, userId)
        whisperHandler.disposeWhisper(roomId, userId)
        if (participant != null) {
            updateOnLeave(participant)
            roomSessionManager.removeUserSession(roomId, userId)
            roomService.updateRoomStatus(roomId)
            roomService.updateCurrentParticipantCount(roomId)
            broadcastRoomChange(roomId, userId)
        }
    }

    private suspend fun sendConnected(userId: UUID, roomId: UUID, userSession: WebSocketSession) {
        val roomInfo = roomService.getRoomInfo(roomId)
        val videoRoomHandleInfo = roomSessionManager.getUserSession(roomId, userId)?.janusSessionInfo?.videoRoomHandleInfo ?: throw WebSocketException.SessionNotFound()
        val participants = participantService.findCurrentParticipants(roomId)
        val payload = ConnectedRoom(
            roomInfo = roomInfo,
            participants = participants.map { it.toDto() },
            videoRoomHandleInfo = videoRoomHandleInfo
        )
        val message = ServerMessage(type = MessageType.ROOM, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun broadcastRoomChange(roomId: UUID, exceptUserId: UUID) {
        val roomInfo = roomService.getRoomInfo(roomId)
        val participants = participantService.findCurrentParticipants(roomId)
        val payload = ChangedRoom(
            roomInfo = roomInfo,
            participants = participants.map { it.toDto() },
        )
        val message = ServerMessage(type = MessageType.ROOM, payload = payload)
        roomSessionManager.broadcastMessageToRoom(roomId, message, setOf(exceptUserId))
    }

    private suspend fun updateOnLeave(participant: RoomParticipantSession) {
        participantService.updateParticipantOnLeave(participant.participantId)
        historyService.updateCallHistoryOnLeave(participant.historyId)
    }
}