package com.youhajun.transcall.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.video.PeerType

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface VideoRoomBody {
    val request: VideoRoomRequestType
}

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface JoinBody : VideoRoomBody {
    val janusRoomId: Long
    val ptype: PeerType
}

data object CreateRoomBody : VideoRoomBody {
    override val request = VideoRoomRequestType.CREATE
}

data class JoinPublisherBody(
    @JsonProperty("room")
    override val janusRoomId: Long,
    val display: String
) : JoinBody {
    override val ptype = PeerType.PUBLISHER
    override val request = VideoRoomRequestType.JOIN
}

data class JoinSubscriberBody(
    @JsonProperty("room")
    override val janusRoomId: Long,
    @JsonProperty("private_id")
    val privateId: Long?,
    val streams: List<SubscribeStreamBody>,
    @JsonProperty("autoupdate")
    val autoUpdate: Boolean = true,
) : JoinBody {
    override val ptype = PeerType.SUBSCRIBER
    override val request = VideoRoomRequestType.JOIN
}

data class PublishBody(
    @JsonProperty("audiocodec")
    val audioCodec: String? = null,
    @JsonProperty("videocodec")
    val videoCodec: String? = null,
    val bitrate: Int? = null,
    val record: Boolean? = null,
    val filename: String? = null,
    val display: String? = null,
    val metadata: Map<String, Any>? = null,
    @JsonProperty("audio_level_average")
    val audioLevelAverage: Int? = null,
    @JsonProperty("audio_active_packets")
    val audioActivePackets: Int? = null,
    val descriptions: List<StreamDescription>? = null
) : VideoRoomBody {
    override val request = VideoRoomRequestType.PUBLISH
}

data object UnpublishBody : VideoRoomBody {
    override val request = VideoRoomRequestType.UNPUBLISH
}

data object LeaveBody : VideoRoomBody {
    override val request = VideoRoomRequestType.LEAVE
}

data object StartBody : VideoRoomBody {
    override val request = VideoRoomRequestType.START
}

data class UpdateBody(
    val subscribe: List<SubscribeStreamBody>? = null,
    val unsubscribe: List<SubscribeStreamBody>? = null,
) : VideoRoomBody {
    override val request = VideoRoomRequestType.UPDATE
}

data class SubscribeStreamBody(
    @JsonProperty("feed")
    val feedId: Long,
    val mid: String,
    val crossrefid: String
)

data class StreamDescription(
    val mid: String,
    val description: String
)
