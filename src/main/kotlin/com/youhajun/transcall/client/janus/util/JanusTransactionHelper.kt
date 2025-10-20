package com.youhajun.transcall.client.janus.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.client.janus.dto.BaseJanusRequest
import com.youhajun.transcall.client.janus.dto.JanusResponseType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.withTimeout
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

@Component
final class JanusTransactionHelper(
    val objectMapper: ObjectMapper
) {

    val logger: Logger = LogManager.getLogger(JanusTransactionHelper::class.java)

    val transactionMap = ConcurrentHashMap<String, CompletableDeferred<JsonNode>>()

    suspend fun sendJanusMessage(session: WebSocketSession, request: BaseJanusRequest): Result<Unit> {
        return runCatching {
            val payload = objectMapper.writeValueAsString(request)
            logger.info("Sending Janus Message: {}", payload)
            session.send(Mono.just(session.textMessage(payload))).awaitFirstOrNull()
        }
    }

    suspend inline fun <reified T> requestJanusResponse(
        session: WebSocketSession,
        request: BaseJanusRequest
    ): Result<T> {
        val transactionId = request.transaction
        val deferred = CompletableDeferred<JsonNode>()
        transactionMap.putIfAbsent(transactionId, deferred)

        return runCatching {
            val payload = objectMapper.writeValueAsString(request)
            logger.info("Sending Janus Request: {}", payload)
            session.send(Mono.just(session.textMessage(payload))).awaitFirstOrNull()

            val responseJson = withTimeout(5000) { deferred.await() }
            responseJson.janusResponseMapper<T>(objectMapper)
        }.onFailure {
            deferred.completeExceptionally(it)
        }.also {
            transactionMap.remove(transactionId)
        }
    }

    fun receiveJanusResponse(payloadText: String) {
        val json = objectMapper.readTree(payloadText)
        val janusType = json["janus"]?.asText()
        val transaction = json["transaction"]?.asText()
        if (JanusResponseType.from(janusType) != JanusResponseType.ACK && transaction != null) {
            transactionMap[transaction]?.complete(json)
        }
    }
}