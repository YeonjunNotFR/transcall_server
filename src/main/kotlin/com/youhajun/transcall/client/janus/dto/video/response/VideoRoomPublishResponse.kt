package com.youhajun.transcall.client.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class VideoRoomPublishResponse(
    @JsonProperty("videoroom")
    val videoRoom: VideoRoomResponseType,
    val configured: String,
)