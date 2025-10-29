package com.youhajun.transcall.janus.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusResponseType
import com.youhajun.transcall.janus.dto.error.JanusErrorResponse
import com.youhajun.transcall.janus.exception.JanusException
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

    suspend inline fun <reified T : BaseJanusResponse> requestJanusResponse(
        session: WebSocketSession,
        request: BaseJanusRequest
    ): Result<T> = runCatching {
        val transactionId = request.transaction
        val deferred = CompletableDeferred<JsonNode>().also {
            transactionMap[transactionId] = it
        }

        try {
            val payload = objectMapper.writeValueAsString(request)
            logger.info("Sending Janus Request: {}", payload)
            session.send(Mono.just(session.textMessage(payload))).awaitFirstOrNull()

            val responseJson = withTimeout(5000) { deferred.await() }
            parseJanusResponse<T>(responseJson, objectMapper)
        } finally {
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

    inline fun <reified T> parseJanusResponse(
        json: JsonNode,
        objectMapper: ObjectMapper
    ): T {
        val janusType = json["janus"]?.asText()
        if (janusType == JanusResponseType.ERROR.value) {
            val error = objectMapper.convertValue(json, JanusErrorResponse::class.java)
            throw JanusException.JanusResponseException(error)
        }
        return objectMapper.treeToValue(json, T::class.java)
    }
}