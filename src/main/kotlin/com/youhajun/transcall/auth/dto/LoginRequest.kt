package com.youhajun.transcall.auth.dto

import com.youhajun.transcall.user.domain.SocialType

data class LoginRequest(
    val socialType: SocialType,
    val authorizationCode: String
)