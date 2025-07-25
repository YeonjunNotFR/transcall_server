package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
import java.util.*

interface UserQuotaRepositoryCustom {
    suspend fun findByUserPublicId(userPublicId: UUID): UserQuota?
}