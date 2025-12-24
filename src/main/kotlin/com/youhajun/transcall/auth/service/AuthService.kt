package com.youhajun.transcall.auth.service

import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.dto.LoginRequest
import com.youhajun.transcall.auth.dto.NonceResponse

interface AuthService {
    suspend fun loginOrCreate(loginRequest: LoginRequest, remoteAddr: String?): JwtTokenResponse
    suspend fun reissueToken(offerRefreshToken: String, remoteAddr: String?): JwtTokenResponse
    suspend fun generateNonce(): NonceResponse
}
