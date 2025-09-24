package com.youhajun.transcall.auth.repository

interface LoginNonceRepository {

    suspend fun saveNonce(loginRequestId: String, nonce: String, ttlSeconds: Long): Boolean

    suspend fun getAndDeleteNonce(loginRequestId: String): String
}