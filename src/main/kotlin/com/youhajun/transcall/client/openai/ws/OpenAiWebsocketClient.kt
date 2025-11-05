package com.youhajun.transcall.client.openai.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.client.openai.OpenAiConfig
import com.youhajun.transcall.whisper.ws.WhisperConnection
import com.youhajun.transcall.whisper.ws.WhisperWebSocketHandler
import com.youhajun.transcall.user.domain.LanguageType
import kotlinx.coroutines.CompletableDeferred
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.net.URI

@Component
class OpenAiWebsocketClient(
    private val objectMapper: ObjectMapper,
    private val openAiConfig: OpenAiConfig
) {
    companion object {
        private const val REALTIME_URL = "wss://api.openai.com/v1/realtime?model=gpt-4o-realtime-preview"
    }

    suspend fun connect(): OpenAiConnection {
        val connectionDeferred = CompletableDeferred<OpenAiConnection>()
        val client = ReactorNettyWebSocketClient()
        val handler = OpenAiWebsocketHandler(objectMapper, connectionDeferred)
        val uri = URI.create(REALTIME_URL)
        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer ${openAiConfig.apiKey}")
            set(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        client.execute(uri, headers, handler).subscribe()
        return connectionDeferred.await()
    }

    private fun LanguageType.toWhisperPort(): Int = when (this) {
        LanguageType.ENGLISH -> 2000
        LanguageType.KOREAN -> 2001
        else -> 2100
    }
}
