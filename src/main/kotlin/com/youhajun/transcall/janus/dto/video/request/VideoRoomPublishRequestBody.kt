package com.youhajun.transcall.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VideoRoomPublishRequestBody(
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
    val descriptions: List<JanusStreamDescription>? = null
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.PUBLISH
}

data class JanusStreamDescription(
    val mid: String,
    val description: String
)