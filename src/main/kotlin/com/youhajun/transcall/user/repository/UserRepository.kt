package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.User
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CoroutineCrudRepository<User, Long>, UserRepositoryCustom