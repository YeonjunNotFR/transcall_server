package com.youhajun.transcall.janus;

import com.youhajun.transcall.janus.dto.JanusAckResponse
import com.youhajun.transcall.janus.dto.JanusResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.dto.JanusRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withTimeout
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Mono
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

@Component
class JanusWebSocketClient(
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val JANUS_WEBSOCKET_URL = "ws://localhost:8188/janus"
    }

    private val logger = LogManager.getLogger(JanusWebSocketClient::class.java)
    private val transactions = ConcurrentHashMap<String, TransactionState>()
    private val client = ReactorNettyWebSocketClient()

    suspend fun connect(onEvent: suspend (JanusResponse) -> Unit): WebSocketSession {
        val url = URI.create(JANUS_WEBSOCKET_URL)
        val sessionDeferred = CompletableDeferred<WebSocketSession>()
        val handler = object : WebSocketHandler {
            override fun getSubProtocols(): MutableList<String> {
                return mutableListOf("janus-protocol")
            }

            override fun handle(session: WebSocketSession): Mono<Void> {
                sessionDeferred.complete(session)

                return mono {
                    session.receive()
                        .map { it.payloadAsText }
                        .asFlow()
                        .collect { msg ->
                            handleIncomingRawMessage(msg, onEvent)
                        }
                }.then()
            }

        }

        client.execute(url, handler).subscribe()

        return withTimeout(5000) { sessionDeferred.await() }
    }

    suspend fun sendRequest(session: WebSocketSession, request: JanusRequest<*>): JanusResponse {
        val transactionId = request.transaction
        val deferred = CompletableDeferred<JanusResponse>()
        val passArk = request.janus != "keepalive"
        transactions[transactionId] = TransactionState(deferred, passArk)

        val payload = objectMapper.writeValueAsString(request)
        logger.info("Sending to Janus: $payload")
        session.send(Mono.just(session.textMessage(payload))).awaitSingleOrNull()

        return try {
            withTimeout(5000) {
                deferred.await()
            }
        } catch (e: Exception) {
            transactions.remove(transactionId)
            throw JanusException.JanusTimeoutException()
        }
    }

    private suspend fun handleIncomingRawMessage(payload: String, onEvent: suspend (JanusResponse) -> Unit) {
        val message = runCatching {
            logger.info("Janus 응답: $payload")
            objectMapper.readValue(payload, JanusResponse::class.java)
        }.onFailure {
            logger.error("Janus 응답 파싱 실패: $payload", it)
        }.getOrNull() ?: return


        val transactionId = message.transaction
        val state = transactionId?.let { transactions[it] }

        if (state != null) {
            if (message is JanusAckResponse && state.passArk) return

            transactions.remove(transactionId)?.deferred?.complete(message)
        } else {
            onEvent(message)
        }
    }

    private data class TransactionState(
        val deferred: CompletableDeferred<JanusResponse>,
        val passArk: Boolean = true,
    )
}
