package com.youhajun.transcall.client.janus.dto.video

data class VideoRoomJsep(
    val type: JSEPType,
    val sdp: String
)