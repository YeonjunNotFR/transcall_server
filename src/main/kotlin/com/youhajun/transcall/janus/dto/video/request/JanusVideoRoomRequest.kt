package com.youhajun.transcall.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.JanusCommand
import com.youhajun.transcall.janus.dto.video.VideoRoomJsep

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JanusVideoRoomRequest<T : Any>(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("handle_id")
    val handleId: Long,
    val body: T,
    val jsep: VideoRoomJsep? = null
) : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.MESSAGE
}