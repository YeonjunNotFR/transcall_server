package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.RefreshToken
import java.util.*

interface RefreshTokenRepositoryCustom {
    suspend fun findByToken(token: String): RefreshToken?
    suspend fun deleteByUserPublicId(userPublicId: UUID)
}