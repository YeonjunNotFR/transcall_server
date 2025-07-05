package com.youhajun.transcall.auth.google

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties("oauth.google")
@Validated
class GoogleAuthConfig {
    @NotBlank
    var clientId: String = ""

    @NotBlank
    var clientSecret: String = ""

    var redirectUri: String = ""
}