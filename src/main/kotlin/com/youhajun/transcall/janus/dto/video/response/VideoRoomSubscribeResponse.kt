package com.youhajun.transcall.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class VideoRoomSubscribeResponse(
    @JsonProperty("videoroom")
    val videoRoom: VideoRoomResponseType,
    val room: String,
    val streams: List<SubscribeStreamResponse> = emptyList(),
)