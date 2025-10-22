package com.youhajun.transcall.janus.dto.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.JanusCommand

data class KeepAliveRequest(
    @JsonProperty("session_id")
    val sessionId: Long
) : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.KEEPALIVE
}