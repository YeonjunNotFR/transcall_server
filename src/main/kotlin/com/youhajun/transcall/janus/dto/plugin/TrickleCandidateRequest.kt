package com.youhajun.transcall.janus.dto.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.JanusCommand

data class TrickleCandidateRequest<T : TrickleBody>(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("handle_id")
    val handleId: Long,
    val candidate: T
) : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.TRICKLE
}

sealed interface TrickleBody

data class TrickleCandidateBody(
    val candidate: String,
    val sdpMLineIndex: Int,
    val sdpMid: String?,
) : TrickleBody

data class TrickleCompletedBody(
    val completed: Boolean = true
) : TrickleBody