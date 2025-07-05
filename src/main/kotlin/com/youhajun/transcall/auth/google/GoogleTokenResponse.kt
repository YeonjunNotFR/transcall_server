package com.youhajun.transcall.auth.google

data class GoogleTokenResponse(
    val access_token: String,
    val expires_in: Long,
    val refresh_token: String?,
    val scope: String,
    val token_type: String,
    val id_token: String
)