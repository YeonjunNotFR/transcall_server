package com.youhajun.transcall.client.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonProperty

data class CreateVideoRoomRequest(
    @JsonProperty("room")
    val janusRoomId: Long
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.CREATE
}