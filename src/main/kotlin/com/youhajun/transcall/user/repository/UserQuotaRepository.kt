package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserQuotaRepository : CoroutineCrudRepository<UserQuota, Long>, UserQuotaRepositoryCustom