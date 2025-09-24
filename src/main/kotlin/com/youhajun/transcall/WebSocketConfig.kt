package com.youhajun.transcall

import com.youhajun.transcall.ws.handler.RoomWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class WebSocketConfig {

    @Bean
    fun handlerMapping(
        roomWebSocketHandler: RoomWebSocketHandler,
    ): SimpleUrlHandlerMapping {
        val map = mapOf(
            "/ws/room" to roomWebSocketHandler,
        )

        return SimpleUrlHandlerMapping().apply {
            urlMap = map
            order = -1
        }
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}