package com.youhajun.transcall.call.room.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.*

@Repository
class JanusRoomIdCacheRepositoryImpl(
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate
) : JanusRoomIdCacheRepository {

    private val keyPrefix = "janusRoomId:"

    override suspend fun getJanusRoomId(roomId: UUID): Long? {
        val key = "$keyPrefix$roomId"
        return reactiveRedisTemplate.opsForValue().get(key).awaitSingleOrNull()?.toLong()
    }

    override suspend fun saveJanusRoomId(roomId: UUID, janusRoomId: Long, ttl: Duration): Boolean {
        val key = "$keyPrefix$roomId"
        return reactiveRedisTemplate.opsForValue().set(key, janusRoomId.toString(), ttl).awaitSingleOrNull() ?: false
    }

    override suspend fun deleteJanusRoomId(roomId: UUID) {
        val key = "$keyPrefix$roomId"
        reactiveRedisTemplate.delete(key).awaitSingleOrNull()
    }
}
