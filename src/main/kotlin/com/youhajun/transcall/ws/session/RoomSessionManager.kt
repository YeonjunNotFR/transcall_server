package com.youhajun.transcall.ws.session

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.vo.RoomParticipantSession
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
    private val userSessionsPerRoom: MutableMap<UUID, MutableMap<UUID, RoomParticipantSession>> = ConcurrentHashMap()

    suspend fun addUserSession(roomId: UUID, session: RoomParticipantSession) {
        val userSessions = userSessionsPerRoom.computeIfAbsent(roomId) { ConcurrentHashMap() }
        userSessions[session.userId] = session
    }

    suspend fun removeUserSession(roomId: UUID, userId: UUID) {
        userSessionsPerRoom[roomId]?.remove(userId)

        if (userSessionsPerRoom[roomId]?.isEmpty() == true) {
            userSessionsPerRoom.remove(roomId)
        }
    }

    suspend fun updateUserSession(roomId: UUID, userId: UUID, newSession: (RoomParticipantSession) -> RoomParticipantSession) = lock.withLock {
        val session = getUserSession(roomId, userId)
        if(session != null) {
            userSessionsPerRoom[roomId]?.set(userId, newSession(session))
        }
    }

    fun getUserSession(roomId: UUID, userId: UUID): RoomParticipantSession? {
        return userSessionsPerRoom[roomId]?.get(userId)
    }

    fun getUsersSession(roomId: UUID): List<RoomParticipantSession> {
        return userSessionsPerRoom[roomId]?.values?.toList() ?: emptyList()
    }

    suspend fun broadcastMessageToRoom(roomId: UUID, message: ServerMessage, exceptUserIds: Set<UUID> = emptySet()) {
        userSessionsPerRoom[roomId]
            ?.filterNot { it.key in exceptUserIds }
            ?.forEach { session ->
                session.value.userSession.sendServerMessage(message, objectMapper)
            }
    }
}
