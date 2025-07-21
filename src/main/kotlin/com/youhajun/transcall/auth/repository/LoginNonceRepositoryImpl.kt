package com.youhajun.transcall.auth.repository

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration

@Repository
class LoginNonceRepositoryImpl(
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate
):LoginNonceRepository {

    private val keyPrefix = "nonce:"

    override fun saveNonce(loginRequestId: String, nonce: String, ttlSeconds: Long): Mono<Boolean> {
        val key = "$keyPrefix$loginRequestId"
        return reactiveRedisTemplate.opsForValue().set(key, nonce, Duration.ofSeconds(ttlSeconds))
    }

    override fun getAndDeleteNonce(loginRequestId: String): Mono<String> {
        val key = "$keyPrefix$loginRequestId"
        return reactiveRedisTemplate.opsForValue().getAndDelete(key)
    }
}
