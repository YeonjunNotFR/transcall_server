package com.youhajun.transcall.janus.dto.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusCommand
import com.youhajun.transcall.janus.dto.JanusResponseType

data class AttachRequest(
    val plugin: String,
    @JsonProperty("session_id")
    val sessionId: Long
) : BaseJanusRequest() {
    override val janus = JanusCommand.ATTACH
}

data class AttachResponse(
    val transaction: String,
    val data: AttachData,
    @JsonProperty("session_id") val sessionId: Long
) : BaseJanusResponse {
    override val janus = JanusResponseType.SUCCESS
}

data class AttachData(
    @JsonProperty("id") val handleId: Long
)