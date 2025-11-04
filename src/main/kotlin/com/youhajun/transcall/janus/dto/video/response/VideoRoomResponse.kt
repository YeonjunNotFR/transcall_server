package com.youhajun.transcall.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "videoroom"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JoinPublisherResponse::class, name = "joined"),
    JsonSubTypes.Type(value = VideoRoomEventResponse::class, name = "event"),
    JsonSubTypes.Type(value = VideoRoomSubscribeUpdateResponse::class, name = "updated"),
    JsonSubTypes.Type(value = VideoRoomSubscribeAttachedResponse::class, name = "attached")
)
sealed interface VideoRoomResponse {
    val videoRoom: VideoRoomResponseType
}

data class JoinPublisherResponse(
    @JsonProperty("room") val
    roomId: String,
    @JsonProperty("id") val
    feedId: Long,
    @JsonProperty("private_id")
    val privateId: Long?,
    val publishers: List<JanusPublisherResponse> = emptyList()
) : VideoRoomResponse {
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType = VideoRoomResponseType.JOINED
}

data class VideoRoomEventResponse(
    val configured: String? = null,
    val started: String? = null
) : VideoRoomResponse {
    fun isSuccess() = configured == "ok" || started == "ok"
    override val videoRoom = VideoRoomResponseType.EVENT
}

data class VideoRoomSubscribeUpdateResponse(
    @JsonProperty("room")
    val roomId: String,
    val streams: List<SubscribeStreamResponse> = emptyList()
) : VideoRoomResponse {
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType = VideoRoomResponseType.UPDATED
}

data class VideoRoomSubscribeAttachedResponse(
    @JsonProperty("room")
    val roomId: String,
    val streams: List<SubscribeStreamResponse> = emptyList()
) : VideoRoomResponse {
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType = VideoRoomResponseType.ATTACHED
}

data class JanusPublisherResponse(
    @JsonProperty("id")
    val feedId: Long,
    val display: String? = null,
    val metadata: Map<String, Any>? = null,
    val streams: List<PublishStreamResponse> = emptyList()
)

data class PublishStreamResponse(
    val type: String? = null,
    val mid: String? = null,
    val mindex: Int? = null,
    val description: String? = null,
    val codec: String? = null
)

data class SubscribeStreamResponse(
    val type: String? = null,
    val active: Boolean? = null,
    val mindex: Int? = null,
    val mid: String? = null,
    val crossrefid: String? = null,
    val ready: Boolean? = null,
    val send: Boolean? = null,
    @JsonProperty("feed_id") val
    feedId: Long? = null,
    @JsonProperty("feed_display")
    val feedDisplay: String? = null,
    @JsonProperty("feed_mid")
    val feedMid: String? = null,
    @JsonProperty("feed_description")
    val feedDescription: String? = null,
    val codec: String? = null
)
