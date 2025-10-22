package com.youhajun.transcall.janus.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.JanusResponseType
import com.youhajun.transcall.janus.dto.error.JanusErrorResponse
import com.youhajun.transcall.janus.exception.JanusException

inline fun <reified T>JsonNode.janusResponseMapper(objectMapper: ObjectMapper): T {
    val janusType = this["janus"]?.asText()
    if (JanusResponseType.from(janusType) == JanusResponseType.ERROR) {
        val errorResponse = objectMapper.convertValue(this, JanusErrorResponse::class.java)
        throw JanusException.JanusResponseException(errorResponse)
    }

    return objectMapper.readValue(objectMapper.treeAsTokens(this), object : TypeReference<T>() {})
}