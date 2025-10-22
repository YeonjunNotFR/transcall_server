package com.youhajun.transcall.janus.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusResponseType
import com.youhajun.transcall.janus.dto.plugin.JanusPluginData
import com.youhajun.transcall.janus.dto.video.VideoRoomJsep

data class JanusPluginEvent<T : PluginEvent>(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("sender")
    val handleId: Long,
    @JsonProperty("plugindata")
    val pluginData: JanusPluginData<T>,
    val jsep: VideoRoomJsep?
) : BaseJanusResponse, JanusEvent {
    override val janus: JanusResponseType = JanusResponseType.EVENT
}