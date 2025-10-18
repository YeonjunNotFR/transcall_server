package com.youhajun.transcall.client.janus.dto.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.client.janus.dto.BaseJanusResponse
import com.youhajun.transcall.client.janus.dto.JanusResponseType

data class AttachResponse(
    val transaction: String,
    val data: AttachData,
    @JsonProperty("session_id")
    val sessionId: Long,
) : BaseJanusResponse {
    override val janus: JanusResponseType = JanusResponseType.SUCCESS
}

data class AttachData(
    @JsonProperty("id")
    val handleId: Long
)