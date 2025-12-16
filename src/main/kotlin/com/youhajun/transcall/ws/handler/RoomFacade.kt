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
        val roomTitle = roomService.getRoomInfo(roomId).title
        val historyId = historyService.saveCallHistory(userId, roomId, roomTitle)
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
        roomService.updateRoomStatusAndCount(roomId)

        val context = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        sendConnectedMessage(context, roomId)
        broadcastRoomChange(roomId, userId)
    }

    suspend fun processLeave(roomId: UUID, userId: UUID) {
        val context = roomSessionManager.getUserSession(roomId, userId) ?: return

        participantService.updateParticipantOnLeave(context.participantId)
        historyService.updateCallHistoryOnLeave(context.historyId)
        roomService.updateRoomStatusAndCount(roomId)
        roomSessionManager.removeUserSession(roomId, userId)

        broadcastRoomChange(roomId, userId)
    }

    private suspend fun sendConnectedMessage(context: RoomParticipantContext, roomId: UUID) {
        val roomInfo = roomService.getRoomInfoWithCurrentParticipants(roomId)
        val payload = ConnectedRoom(roomInfo = roomInfo.roomInfo, participants = roomInfo.participants)

        val message = ServerMessage(type = MessageType.ROOM, payload = payload)
        context.userSession.sendServerMessage(message, objectMapper)
    }


    private suspend fun broadcastRoomChange(roomId: UUID, userId: UUID) {
        val roomInfo = roomService.getRoomInfoWithCurrentParticipants(roomId)
        val payload = ChangedRoom(roomInfo = roomInfo.roomInfo, participants = roomInfo.participants)

        val message = ServerMessage(type = MessageType.ROOM, payload = payload)
        roomSessionManager.broadcastMessageToRoom(roomId, message, setOf(userId))
    }
}