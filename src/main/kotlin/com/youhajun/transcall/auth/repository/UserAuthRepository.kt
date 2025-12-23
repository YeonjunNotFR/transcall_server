package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.UserAuth
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface UserAuthRepository : CoroutineCrudRepository<UserAuth, UUID>, UserAuthRepositoryCustom
