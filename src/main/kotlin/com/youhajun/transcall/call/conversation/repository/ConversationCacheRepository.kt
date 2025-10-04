package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.ConversationCache
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.util.*

interface ConversationCacheRepository {
    suspend fun publishCache(roomId: UUID, userId: UUID, cache: ConversationCache, ttl: Duration): Long?
    suspend fun getConversationCache(roomId: UUID, userId: UUID): ConversationCache?

    fun subscribeCache(roomId: UUID, userId: UUID): Flow<ConversationCache>
}