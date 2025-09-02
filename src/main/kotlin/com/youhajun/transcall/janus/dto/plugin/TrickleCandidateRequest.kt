package com.youhajun.transcall.janus.dto.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.JanusCommand

data class TrickleCandidateRequest<T>(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("handle_id")
    val handleId: Long,
    val candidate: T
) : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.TRICKLE
}

data class TrickleCandidateBody(
    val candidate: String,
    val sdpMid: String,
    val sdpMLineIndex: Int
)

data class TrickleCompletedBody(
    val completed: Boolean = true
)