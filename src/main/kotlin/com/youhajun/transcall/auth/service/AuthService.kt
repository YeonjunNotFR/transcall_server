package com.youhajun.transcall.auth.service

import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.dto.LoginRequest

interface AuthService {
    suspend fun loginOrCreate(loginRequest: LoginRequest): JwtTokenResponse
    suspend fun reissueToken(offerRefreshToken: String): JwtTokenResponse
}
