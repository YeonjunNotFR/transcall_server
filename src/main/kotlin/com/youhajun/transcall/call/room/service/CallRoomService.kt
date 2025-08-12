package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import java.util.*

interface CallRoomService {
    suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID
}