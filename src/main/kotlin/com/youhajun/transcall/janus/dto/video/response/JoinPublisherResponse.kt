package com.youhajun.transcall.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class JoinPublisherResponse(
    @JsonProperty("videoroom")
    val videoRoom: VideoRoomResponseType,
    @JsonProperty("room")
    val roomId: String,
    @JsonProperty("id")
    val feedId: Long,
    @JsonProperty("private_id")
    val privateId: Long?,
    val publishers: List<JanusPublisherResponse> = emptyList()
)