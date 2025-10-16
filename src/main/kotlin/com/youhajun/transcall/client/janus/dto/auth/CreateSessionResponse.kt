package com.youhajun.transcall.client.janus.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.client.janus.dto.BaseJanusResponse
import com.youhajun.transcall.client.janus.dto.JanusResponseType

data class CreateSessionResponse(
    val transaction: String,
    val data: CreateSessionData
) : BaseJanusResponse {
    override val janus: JanusResponseType = JanusResponseType.SUCCESS
}

data class CreateSessionData(
    @JsonProperty("id")
    val sessionId: Long
)