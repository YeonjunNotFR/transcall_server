package com.youhajun.transcall.user.service

import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User

interface UserService {

    suspend fun findOrCreateUser(email: String, socialType: SocialType): User

    suspend fun isEmailExist(email: String): Boolean

    suspend fun findUserByPublicId(publicId: String): User
}
