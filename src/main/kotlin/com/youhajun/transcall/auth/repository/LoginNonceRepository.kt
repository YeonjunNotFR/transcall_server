package com.youhajun.transcall.auth.repository

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class LoginNonceRepository(
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate
) {

    private val keyPrefix = "nonce:"

    fun saveNonce(loginRequestId: String, nonce: String, ttlSeconds: Long = 300): Mono<Boolean> {
        val key = "$keyPrefix$loginRequestId"
        return reactiveRedisTemplate.opsForValue().set(key, nonce, Duration.ofSeconds(ttlSeconds))
    }

    fun getAndDeleteNonce(loginRequestId: String): Mono<String> {
        val key = "$keyPrefix$loginRequestId"
        return reactiveRedisTemplate.opsForValue().getAndDelete(key)
    }
}
