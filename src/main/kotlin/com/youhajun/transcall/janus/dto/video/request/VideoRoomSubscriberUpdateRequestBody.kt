package com.youhajun.transcall.janus.dto.video.request

data class VideoRoomSubscriberUpdateRequestBody(
    val subscribe: List<SubscribeStreamRequest>,
    val unsubscribe: List<SubscribeStreamRequest>,
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.UPDATE
}