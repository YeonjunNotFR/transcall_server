package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.history.service.CallHistoryService
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.user.service.UserService
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.ChangedRoom
import com.youhajun.transcall.ws.dto.payload.ConnectedRoom
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MessageType
import com.youhajun.transcall.ws.vo.RoomParticipantContext
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

@Service
class RoomFacade(
    private val userService: UserService,
    private val roomService: CallRoomService,
    private val participantService: CallParticipantService,
    private val historyService: CallHistoryService,
    private val roomSessionManager: RoomSessionManager,
    private val objectMapper: ObjectMapper
) {

    suspend fun prepareEntry(roomId: UUID, userId: UUID, session: WebSocketSession) {
        if (roomService.isRoomFull(roomId)) {
            throw WebSocketException.RoomFull()
        }

        val user = userService.findUserById(userId)
        val historyId = historyService.saveCallHistory(userId, roomId)
        val participantId = participantService.saveParticipant(roomId, userId)

        val context = RoomParticipantContext(
            userId = userId,
            userSession = session,
            historyId = historyId,
            participantId = participantId,
            language = user.language
        )

        roomSessionManager.addUserSession(roomId, context)
    }

    suspend fun completeEntry(roomId: UUID, userId: UUID) {
        roomService.updateRoomStatus(roomId)
        roomService.updateCurrentParticipantCount(roomId)

        val context = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        sendConnectedMessage(context, roomId)
        broadcastRoomChange(roomId, userId)
    }

    suspend fun processLeave(roomId: UUID, userId: UUID) {
        val context = roomSessionManager.getUserSession(roomId, userId) ?: return

        participantService.updateParticipantOnLeave(context.participantId)
        historyService.updateCallHistoryOnLeave(context.historyId)

        roomSessionManager.removeUserSession(roomId, userId)

        roomService.updateRoomStatus(roomId)
        roomService.updateCurrentParticipantCount(roomId)

        broadcastRoomChange(roomId, userId)
    }

    private suspend fun sendConnectedMessage(context: RoomParticipantContext, roomId: UUID) {
        val roomInfo = roomService.getRoomInfo(roomId)
        val participants = participantService.findCurrentParticipants(roomId)

        val payload = ConnectedRoom(
            roomInfo = roomInfo,
            participants = participants.map { it.toDto() },
        )

        val message = ServerMessage(type = MessageType.ROOM, payload = payload)
        context.userSession.sendServerMessage(message, objectMapper)
    }


    private suspend fun broadcastRoomChange(roomId: UUID, exceptUserId: UUID) {
        val roomInfo = roomService.getRoomInfo(roomId)
        val participants = participantService.findCurrentParticipants(roomId)
        val payload = ChangedRoom(roomInfo = roomInfo, participants = participants.map { it.toDto() })

        val message = ServerMessage(type = MessageType.ROOM, payload = payload)
        roomSessionManager.broadcastMessageToRoom(roomId, message, setOf(exceptUserId))
    }
}