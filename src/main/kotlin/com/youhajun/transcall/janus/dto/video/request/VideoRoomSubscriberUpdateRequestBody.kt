package com.youhajun.transcall.janus.dto.video.request

data class VideoRoomSubscriberUpdateRequestBody(
    val subscribe: List<StreamInfoRequest>,
    val unsubscribe: List<StreamInfoRequest>,
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.UPDATE
}