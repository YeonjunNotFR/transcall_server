package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.ws.dto.ClientMessage
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.*
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MediaStateInfo
import com.youhajun.transcall.ws.vo.MessageType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

@Component
class MediaStateMessageHandler(
    private val objectMapper: ObjectMapper,
    private val roomSessionManager: RoomSessionManager,
) : WebSocketMessageHandler {

    override fun supports(type: MessageType): Boolean = type == MessageType.MEDIA_STATE

    override suspend fun handle(userId: UUID, roomId: UUID, session: WebSocketSession, msg: ClientMessage) {
        when (val payload = msg.payload as MediaStateRequest) {
            is CameraEnableChanged -> payload.cameraEnableChanged(userId, roomId)
            is MicEnableChanged -> payload.micEnableChanged(userId, roomId)
        }
    }

    private suspend fun CameraEnableChanged.cameraEnableChanged(userId: UUID, roomId: UUID) {
        updateMediaState(userId, roomId) { it.copy(isCameraEnabled = isEnabled) }
    }

    private suspend fun MicEnableChanged.micEnableChanged(userId: UUID, roomId: UUID) {
        updateMediaState(userId, roomId) { it.copy(isMicEnabled = isEnabled) }
    }

    private suspend fun updateMediaState(userId: UUID, roomId: UUID, update: (MediaStateInfo) -> MediaStateInfo) {
        roomSessionManager.updateUserSession(roomId, userId) {
            it.copy(mediaState = update(it.mediaState))
        }

        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val payload = MediaStateChanged(mediaState = participant.toMediaStateDto())
        val message = ServerMessage(type = MessageType.MEDIA_STATE, payload = payload)

        roomSessionManager.broadcastMessageToRoom(roomId, message, setOf(userId))
    }
}