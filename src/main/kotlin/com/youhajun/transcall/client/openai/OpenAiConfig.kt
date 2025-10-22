package com.youhajun.transcall.client.openai

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("open-ai")
class OpenAiConfig {
    @NotBlank
    var apiKey: String = ""
}