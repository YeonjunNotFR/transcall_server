package com.youhajun.transcall.call.room.repository

import org.springframework.stereotype.Repository
import java.util.*
import java.time.Duration

@Repository
interface JanusRoomIdCacheRepository {

    suspend fun saveJanusRoomId(roomId: UUID, janusRoomId: Long, ttl: Duration): Boolean

    suspend fun getJanusRoomId(roomId: UUID): Long?

    suspend fun deleteJanusRoomId(roomId: UUID)
}