package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserQuotaRepository : CoroutineCrudRepository<UserQuota, UUID>