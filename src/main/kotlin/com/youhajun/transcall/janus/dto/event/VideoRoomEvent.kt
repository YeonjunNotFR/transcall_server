package com.youhajun.transcall.janus.dto.event

import com.youhajun.transcall.janus.dto.video.response.VideoRoomResponseType

sealed interface VideoRoomEvent : PluginEvent {
    val videoRoom: VideoRoomResponseType
}