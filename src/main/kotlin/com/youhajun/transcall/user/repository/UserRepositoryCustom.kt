package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.User

interface UserRepositoryCustom {
    suspend fun findUserByEmail(email: String): User?
    suspend fun existsUserByEmail(email: String): Boolean
}