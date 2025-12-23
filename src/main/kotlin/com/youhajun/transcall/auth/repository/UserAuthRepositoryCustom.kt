package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.UserAuth
import java.time.Instant
import java.util.UUID

interface UserAuthRepositoryCustom {
    suspend fun findByRefreshToken(refreshToken: String): UserAuth?
    suspend fun upsertRefreshToken(userId: UUID, refreshToken: String, expireAt: Instant)
    suspend fun upsertLastLogin(userId: UUID, lastLoginAt: Instant, lastLoginIp: String?)
    suspend fun upsertSocialId(userId: UUID, socialId: String)
}
