package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface UserQuotaRepository : CoroutineCrudRepository<UserQuota, UUID>, UserQuotaRepositoryCustom