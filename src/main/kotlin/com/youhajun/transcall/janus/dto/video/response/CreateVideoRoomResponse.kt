package com.youhajun.transcall.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateVideoRoomResponse(
    @JsonProperty("room")
    val janusRoomId: Long,
) {
    @JsonProperty("videoroom")
    val videoRoom: VideoRoomResponseType = VideoRoomResponseType.CREATED
}