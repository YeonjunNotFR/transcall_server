package com.youhajun.transcall.call.conversation.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.conversation.domain.ConversationCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.*

@Repository
class ConversationCacheRepositoryImpl(
    private val objectMapper: ObjectMapper,
    private val container: ReactiveRedisMessageListenerContainer,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, ConversationCache>
) : ConversationCacheRepository {

    private val logger: Logger = LogManager.getLogger(ConversationCacheRepositoryImpl::class.java)

    private fun keyPrefix(roomId: UUID, senderId: UUID) = "conversation:$roomId:$senderId"

    override suspend fun publishCache(roomId: UUID, userId: UUID, cache: ConversationCache, ttl: Duration): Long? {
        val key = keyPrefix(roomId, userId)
        return reactiveRedisTemplate.opsForValue().set(key, cache, ttl)
            .then(reactiveRedisTemplate.convertAndSend(key, cache))
            .awaitSingleOrNull()
    }

    override suspend fun getConversationCache(roomId: UUID, userId: UUID): ConversationCache? {
        val key = keyPrefix(roomId, userId)
        return reactiveRedisTemplate.opsForValue().get(key).awaitSingleOrNull()
    }

    override fun subscribeCache(roomId: UUID, userId: UUID): Flow<ConversationCache> {
        val key = keyPrefix(roomId, userId)

        return container.receive(listOf(ChannelTopic.of(key)), channelSerializer(), valueSerializer())
            .map { it.message }
            .publish()
            .refCount(1)
            .distinctUntilChanged()
            .asFlow()
    }

    private fun channelSerializer(): RedisSerializationContext.SerializationPair<String> =
        RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())

    private fun valueSerializer(): RedisSerializationContext.SerializationPair<ConversationCache> =
        RedisSerializationContext.SerializationPair.fromSerializer(
            Jackson2JsonRedisSerializer(objectMapper, ConversationCache::class.java)
        )
}