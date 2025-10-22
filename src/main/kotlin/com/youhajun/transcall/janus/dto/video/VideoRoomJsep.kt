package com.youhajun.transcall.janus.dto.video

data class VideoRoomJsep(
    val type: JSEPType,
    val sdp: String
)