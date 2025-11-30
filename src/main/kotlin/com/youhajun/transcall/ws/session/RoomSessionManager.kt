package com.youhajun.transcall.ws.session

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.vo.JanusSessionInfo
import com.youhajun.transcall.ws.vo.RoomParticipantContext
import com.youhajun.transcall.ws.vo.WhisperSessionInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class RoomSessionManager(
    private val objectMapper: ObjectMapper
) {

    private val lock = Mutex()
    private val userSessionsPerRoom: MutableMap<UUID, MutableMap<UUID, RoomParticipantContext>> = ConcurrentHashMap()

    suspend fun addUserSession(roomId: UUID, session: RoomParticipantContext) {
        val userSessions = userSessionsPerRoom.computeIfAbsent(roomId) { ConcurrentHashMap() }
        userSessions[session.userId] = session
    }

    suspend fun removeUserSession(roomId: UUID, userId: UUID) {
        userSessionsPerRoom[roomId]?.remove(userId)

        if (userSessionsPerRoom[roomId]?.isEmpty() == true) {
            userSessionsPerRoom.remove(roomId)
        }
    }

    suspend fun updateUserSession(roomId: UUID, userId: UUID, newSession: (RoomParticipantContext) -> RoomParticipantContext) = lock.withLock {
        val session = getUserSession(roomId, userId)
        if(session != null) {
            userSessionsPerRoom[roomId]?.set(userId, newSession(session))
        }
    }

    fun getUsersSessionInRoom(roomId: UUID): List<RoomParticipantContext> {
        return userSessionsPerRoom[roomId]?.values?.toList() ?: emptyList()
    }

    fun requireContext(roomId: UUID, userId: UUID): RoomParticipantContext = getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()

    fun requireJanusSession(roomId: UUID, userId: UUID): JanusSessionInfo = getUserSession(roomId, userId)?.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

    fun requireWhisperSession(roomId: UUID, userId: UUID): WhisperSessionInfo = getUserSession(roomId, userId)?.whisperSessionInfo ?: throw WebSocketException.SessionNotFound()

    suspend fun broadcastMessageToRoom(roomId: UUID, message: ServerMessage, exceptUserIds: Set<UUID> = emptySet()) {
        userSessionsPerRoom[roomId]
            ?.filterNot { it.key in exceptUserIds }
            ?.forEach { session ->
                session.value.userSession.sendServerMessage(message, objectMapper)
            }
    }

    fun getUserSession(roomId: UUID, userId: UUID): RoomParticipantContext? {
        return userSessionsPerRoom[roomId]?.get(userId)
    }
}
