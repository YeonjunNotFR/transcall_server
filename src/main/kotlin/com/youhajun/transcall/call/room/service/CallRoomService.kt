package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import java.util.*

interface CallRoomService {
    suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID
    suspend fun isRoomFull(roomId: UUID): Boolean
    suspend fun getRoomInfo(roomId: UUID): RoomInfoResponse
    suspend fun getJanusRoomId(roomId: UUID): Long
    suspend fun joinRoomByCode(userId: UUID, roomCode: String): RoomInfoResponse
    suspend fun updateRoomStatus(roomId: UUID): Boolean
}