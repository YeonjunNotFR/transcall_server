package com.youhajun.transcall.auth.dto

import jakarta.validation.constraints.NotEmpty

data class ReissueTokenRequest(
    @NotEmpty
    val refreshToken: String
)