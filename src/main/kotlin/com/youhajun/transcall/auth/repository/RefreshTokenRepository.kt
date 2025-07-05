package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.RefreshToken
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : CoroutineCrudRepository<RefreshToken, Long> {
    suspend fun findByToken(token: String): RefreshToken?
    suspend fun deleteByUserPublicId(publicId: String)
}