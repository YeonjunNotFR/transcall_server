package com.youhajun.transcall.janus.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusResponseType

data class JanusMedia(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("sender")
    val handleId: Long,
    val type: String,
    val receiving: Boolean
) : BaseJanusResponse, JanusEvent {
    override val janus: JanusResponseType = JanusResponseType.MEDIA
}