package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.exception.AuthException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class LoginNonceRepositoryImpl(
    private val reactiveRedisTemplate: ReactiveStringRedisTemplate
):LoginNonceRepository {

    private val keyPrefix = "nonce:"

    override suspend fun saveNonce(loginRequestId: String, nonce: String, ttlSeconds: Long): Boolean {
        val key = "$keyPrefix$loginRequestId"
        return reactiveRedisTemplate.opsForValue().set(key, nonce, Duration.ofSeconds(ttlSeconds)).awaitSingleOrNull() ?: false
    }

    override suspend fun getAndDeleteNonce(loginRequestId: String): String {
        val key = "$keyPrefix$loginRequestId"
        return reactiveRedisTemplate.opsForValue().getAndDelete(key).awaitSingleOrNull() ?: throw AuthException.NonceNotFoundException()
    }
}
