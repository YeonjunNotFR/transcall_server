package com.youhajun.transcall.janus.ws;

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.util.JanusTransactionHelper
import kotlinx.coroutines.CompletableDeferred
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import java.net.URI

@Component
class JanusWebSocketClient(
    private val transactionHelper: JanusTransactionHelper,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val JANUS_WEBSOCKET_URL = "ws://localhost:8188/janus"
    }

    suspend fun connect(): JanusConnection {
        val connectionDeferred = CompletableDeferred<JanusConnection>()
        val handler = JanusWebSocketHandler(transactionHelper, objectMapper, connectionDeferred)
        val headers = HttpHeaders()
        val client = ReactorNettyWebSocketClient()
        val uri = URI.create(JANUS_WEBSOCKET_URL)

        client.execute(uri, headers, handler).subscribe()
        return connectionDeferred.await()
    }
}
