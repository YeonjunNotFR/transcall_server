package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.RefreshToken
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface RefreshTokenRepository : CoroutineCrudRepository<RefreshToken, UUID>, RefreshTokenRepositoryCustom