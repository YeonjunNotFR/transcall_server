package com.youhajun.transcall.janus.dto.plugin

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusResponseType
import com.youhajun.transcall.janus.dto.video.VideoRoomJsep

data class JanusPluginResponse<T : Any>(
    val transaction: String,
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("sender")
    val handleId: Long,
    @JsonProperty("plugindata")
    val pluginData: JanusPluginData<T>,
    val jsep: VideoRoomJsep?
) : BaseJanusResponse {
    override val janus: JanusResponseType = JanusResponseType.EVENT
}

data class JanusPluginData<T : Any>(
    val plugin: String,
    val data: T
)