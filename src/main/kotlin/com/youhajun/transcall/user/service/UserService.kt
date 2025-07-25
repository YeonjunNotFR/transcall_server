package com.youhajun.transcall.user.service

import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User
import com.youhajun.transcall.user.dto.MyInfoResponse
import java.util.UUID

interface UserService {

    suspend fun findOrCreateUser(email: String, socialType: SocialType): User

    suspend fun isEmailExist(email: String): Boolean

    suspend fun findUserByPublicId(publicId: UUID): User

    suspend fun getMyInfo(userPublicId: UUID): MyInfoResponse
}
