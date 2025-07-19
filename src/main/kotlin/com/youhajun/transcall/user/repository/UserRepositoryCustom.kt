package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.User
import java.util.UUID

interface UserRepositoryCustom {
    suspend fun findUserByEmail(email: String): User?
    suspend fun findUserByPublicId(publicId: UUID): User?
    suspend fun existsUserByEmail(email: String): Boolean
}