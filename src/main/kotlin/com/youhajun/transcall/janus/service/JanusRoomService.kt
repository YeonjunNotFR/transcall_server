package com.youhajun.transcall.janus.service

import org.springframework.stereotype.Service

@Service
interface JanusRoomService {
    suspend fun createRoom(sessionId: Long, handleId: Long): Result<Long>
}