package com.youhajun.transcall.auth.controller

import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.dto.LoginRequest
import com.youhajun.transcall.auth.google.GoogleService
import com.youhajun.transcall.auth.service.AuthService
import com.youhajun.transcall.user.domain.SocialType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest): JwtTokenResponse {
        return authService.loginOrCreate(request)
    }

    @PostMapping("/reissue")
    suspend fun reissueToken(@RequestBody refreshToken: String): JwtTokenResponse {
        return authService.reissueToken(refreshToken)
    }
}