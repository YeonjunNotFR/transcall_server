package com.youhajun.transcall.call.room.repository

interface CallRoomRepositoryCustom {
    suspend fun existsByRoomCode(roomCode: String): Boolean
}