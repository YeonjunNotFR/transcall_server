package com.youhajun.transcall.auth.jwt

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties("jwt")
@Validated
class JwtConfig {
    @NotBlank
    var secret: String = ""

    @Min(1)
    var accessTokenValidityMs: Long = 0

    @Min(1)
    var refreshTokenValidityMs: Long = 0
}