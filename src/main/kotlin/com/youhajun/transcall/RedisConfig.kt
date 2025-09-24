package com.youhajun.transcall

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.conversation.domain.ConversationCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val objectMapper: ObjectMapper,
    private val connectionFactory: ReactiveRedisConnectionFactory,
) {

    @Bean
    fun conversationCacheTemplate(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, ConversationCache> {
        val serializer = Jackson2JsonRedisSerializer(objectMapper, ConversationCache::class.java)
        val context = RedisSerializationContext
            .newSerializationContext<String, ConversationCache>(StringRedisSerializer())
            .value(serializer)
            .build()
        return ReactiveRedisTemplate(factory, context)
    }


    @Bean
    fun messageListenerContainer(): ReactiveRedisMessageListenerContainer {
        return ReactiveRedisMessageListenerContainer(connectionFactory)
    }
}