package com.youhajun.transcall.auth.controller

import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.dto.LoginRequest
import com.youhajun.transcall.auth.dto.NonceResponse
import com.youhajun.transcall.auth.dto.ReissueTokenRequest
import com.youhajun.transcall.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/social-login")
    suspend fun socialLogin(@Valid @RequestBody request: LoginRequest): JwtTokenResponse {
        return authService.loginOrCreate(request)
    }

    @GetMapping("/nonce")
    suspend fun loginNonce(): NonceResponse {
        return authService.generateNonce()
    }

    @PostMapping("/reissue")
    suspend fun reissueToken(@Valid @RequestBody request: ReissueTokenRequest): JwtTokenResponse {
        return authService.reissueToken(request.refreshToken)
    }
}