package com.youhajun.transcall.common.config

import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

class AppConfig {
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }
}