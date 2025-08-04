package com.youhajun.transcall.auth.google

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("oauth.google")
class GoogleAuthConfig {
    @NotBlank
    var clientId: String = ""

    @NotBlank
    var clientSecret: String = ""

    var redirectUri: String = ""
}