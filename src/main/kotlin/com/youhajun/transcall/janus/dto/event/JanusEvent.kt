package com.youhajun.transcall.janus.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusResponseType
import com.youhajun.transcall.janus.dto.plugin.JanusPluginData
import com.youhajun.transcall.janus.dto.plugin.TrickleBody
import com.youhajun.transcall.janus.dto.video.VideoRoomJsep

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "janus"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JanusMedia::class, name = "media"),
    JsonSubTypes.Type(value = TrickleCandidateResponse::class, name = "trickle"),
    JsonSubTypes.Type(value = JanusPluginEvent::class, name = "event")
)
sealed interface JanusEvent {
    val janus: JanusResponseType
}

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

data class JanusPluginEvent<T : VideoRoomEvent>(
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

data class TrickleCandidateResponse<T : TrickleBody>(
    @JsonProperty("session_id")
    val sessionId: Long,
    @JsonProperty("sender")
    val handleId: Long,
    val candidate: T
) : BaseJanusResponse, JanusEvent {
    override val janus: JanusResponseType = JanusResponseType.TRICKLE
}