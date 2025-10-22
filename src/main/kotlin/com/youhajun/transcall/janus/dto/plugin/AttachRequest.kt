package com.youhajun.transcall.janus.dto.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.JanusCommand

data class AttachRequest(
    val plugin: String,
    @JsonProperty("session_id")
    val sessionId: Long
) : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.ATTACH
}