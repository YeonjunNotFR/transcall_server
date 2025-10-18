package com.youhajun.transcall.client.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class JoinSubscriberResponse(
    @JsonProperty("videoroom")
    val videoRoom: VideoRoomResponseType,
    val room: String,
    val streams: List<SubscribeStreamResponse> = emptyList(),
)