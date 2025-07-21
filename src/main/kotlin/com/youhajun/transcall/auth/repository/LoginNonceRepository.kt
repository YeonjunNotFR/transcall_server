package com.youhajun.transcall.auth.repository

import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface LoginNonceRepository {

    fun saveNonce(loginRequestId: String, nonce: String, ttlSeconds: Long = 300): Mono<Boolean>

    fun getAndDeleteNonce(loginRequestId: String): Mono<String>
}