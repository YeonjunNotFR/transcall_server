package com.youhajun.transcall.auth.dto

data class JwtTokenResponse(
    val accessToken: String,
    val refreshToken: String
)