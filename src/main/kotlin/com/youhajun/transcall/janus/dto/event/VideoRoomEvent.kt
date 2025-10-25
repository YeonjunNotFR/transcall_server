package com.youhajun.transcall.janus.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.youhajun.transcall.janus.dto.video.response.JanusPublisherResponse
import com.youhajun.transcall.janus.dto.video.response.VideoRoomResponseType

@JsonTypeInfo(
    use = JsonTypeInfo.Id.DEDUCTION,
    include = JsonTypeInfo.As.PROPERTY
)
@JsonSubTypes(
    JsonSubTypes.Type(OnNewPublisher::class),
    JsonSubTypes.Type(OnUnpublished::class),
    JsonSubTypes.Type(OnLeaving::class)
)
sealed interface VideoRoomEvent {
    val videoRoom: VideoRoomResponseType
    val roomId: Long
}

data class OnNewPublisher(
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType,
    @JsonProperty("room")
    override val roomId: Long,
    val publishers: List<JanusPublisherResponse> = emptyList()
) : VideoRoomEvent

data class OnUnpublished(
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType,
    @JsonProperty("room")
    override val roomId: Long,
    @JsonProperty("unpublished")
    val unpublished: Any?
) : VideoRoomEvent

data class OnLeaving(
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType,
    @JsonProperty("room")
    override val roomId: Long,
    @JsonProperty("leaving")
    val leaving: Any?
) : VideoRoomEvent
