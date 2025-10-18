package com.youhajun.transcall.client.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class VideoRoomStartResponse(
    @JsonProperty("videoroom")
    val videoRoom: VideoRoomResponseType,
    val started: String,
) {
    fun isSuccess(): Boolean {
        return started == "ok"
    }
}