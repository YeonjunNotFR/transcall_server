package com.youhajun.transcall.call.room.repository

import java.time.Duration
import java.util.*

interface JanusRoomIdCacheRepository {

    suspend fun saveJanusRoomId(roomId: UUID, janusRoomId: Long, ttl: Duration): Boolean

    suspend fun getJanusRoomId(roomId: UUID): Long?

    suspend fun deleteJanusRoomId(roomId: UUID)
}