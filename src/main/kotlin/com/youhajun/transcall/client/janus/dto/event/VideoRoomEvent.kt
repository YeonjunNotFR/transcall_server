package com.youhajun.transcall.client.janus.dto.event

import com.youhajun.transcall.client.janus.dto.video.response.VideoRoomResponseType

sealed interface VideoRoomEvent : PluginEvent {
    val videoRoom: VideoRoomResponseType
}