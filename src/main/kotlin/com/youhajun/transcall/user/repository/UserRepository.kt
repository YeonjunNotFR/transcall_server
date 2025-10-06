package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface UserRepository : CoroutineCrudRepository<User, UUID>, UserRepositoryCustom