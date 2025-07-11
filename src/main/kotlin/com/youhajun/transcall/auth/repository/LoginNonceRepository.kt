package com.youhajun.transcall.auth.repository

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class LoginNonceRepository(
    private val redisTemplate: StringRedisTemplate
) {

    private val keyPrefix = "nonce:"

    fun saveNonce(loginRequestId: String, nonce: String, ttlSeconds: Long = 300) {
        val key = "$keyPrefix$loginRequestId"
        redisTemplate.opsForValue().set(key, nonce, Duration.ofSeconds(ttlSeconds))
    }

    fun getAndRemoveNonce(loginRequestId: String): String? {
        val key = "$keyPrefix$loginRequestId"
        return redisTemplate.opsForValue().get(key)?.apply {
            redisTemplate.delete(key)
        }
    }
}
