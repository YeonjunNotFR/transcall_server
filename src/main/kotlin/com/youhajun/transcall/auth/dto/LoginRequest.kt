package com.youhajun.transcall.auth.dto

import com.youhajun.transcall.user.domain.SocialType
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @NotBlank
    val socialType: SocialType,
    @NotBlank
    val token: String,
    @NotBlank
    val loginRequestId: String
)