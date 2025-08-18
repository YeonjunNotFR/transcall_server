package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
import java.util.*

interface UserQuotaRepositoryCustom {
    suspend fun findByUserId(userId: UUID): UserQuota
}