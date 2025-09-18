package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomStatus
import java.util.*

interface CallRoomRepositoryCustom {
    suspend fun existsByRoomCode(roomCode: String): Boolean
    suspend fun findByRoomCode(roomCode: String): CallRoom?
    suspend fun updateRoomStatus(roomId: UUID, status: RoomStatus): Boolean
}