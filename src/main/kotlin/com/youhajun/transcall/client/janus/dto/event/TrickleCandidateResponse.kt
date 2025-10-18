package com.youhajun.transcall.client.janus.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.client.janus.dto.BaseJanusResponse
import com.youhajun.transcall.client.janus.dto.JanusResponseType
import com.youhajun.transcall.client.janus.dto.plugin.TrickleBody

data class TrickleCandidateResponse<T : TrickleBody>(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("sender")
    val handleId: Long,
    val candidate: T
) : BaseJanusResponse, JanusEvent {
    override val janus: JanusResponseType = JanusResponseType.TRICKLE
}