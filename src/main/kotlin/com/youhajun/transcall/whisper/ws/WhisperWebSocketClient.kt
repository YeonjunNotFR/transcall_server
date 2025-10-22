package com.youhajun.transcall.whisper.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.user.domain.LanguageType
import io.netty.channel.ChannelOption
import kotlinx.coroutines.CompletableDeferred
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.netty.http.client.HttpClient
import java.net.URI

@Component
class WhisperWebSocketClient(
    private val objectMapper: ObjectMapper,
) {

    suspend fun connect(languageType: LanguageType): WhisperConnection {
        val port = languageType.whisperPort
        val whisperWebsocketUrl = "ws://localhost:$port/asr"
        val connectionDeferred = CompletableDeferred<WhisperConnection>()
        val headers = HttpHeaders()
        val httpClient = HttpClient.create()
            .keepAlive(true)
            .option(ChannelOption.SO_KEEPALIVE, true)
        val client = ReactorNettyWebSocketClient(httpClient)
        val handler = WhisperWebSocketHandler(objectMapper, connectionDeferred)
        val uri = URI.create(whisperWebsocketUrl)

        client.execute(uri, headers, handler).subscribe()
        return connectionDeferred.await()
    }
}
