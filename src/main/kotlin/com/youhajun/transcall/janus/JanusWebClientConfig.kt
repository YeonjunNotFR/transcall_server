package com.youhajun.transcall.janus

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class JanusWebClientConfig {

    @Bean
    fun janusWebClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl("http://localhost:8088")
            .build()
    }
}