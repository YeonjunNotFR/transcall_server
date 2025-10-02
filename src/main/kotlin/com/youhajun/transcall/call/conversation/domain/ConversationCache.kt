package com.youhajun.transcall.call.conversation.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.redis.core.RedisHash
import java.time.Instant
import java.util.*

@RedisHash("ConversationCache")
data class ConversationCache(
    val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    val originText: String,
    val originLanguage: LanguageType,
    val start: String,
    val timestamp: Long = Instant.now().epochSecond
)
