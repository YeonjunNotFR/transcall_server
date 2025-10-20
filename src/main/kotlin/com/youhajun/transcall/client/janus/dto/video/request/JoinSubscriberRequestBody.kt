package com.youhajun.transcall.client.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.client.janus.dto.video.PeerType

data class JoinSubscriberRequestBody(
    val ptype: PeerType = PeerType.SUBSCRIBER,
    @JsonProperty("room")
    val janusRoomId: Long,
    @JsonProperty("private_id")
    val privateId: Long?,
    val streams: List<SubscribeStreamRequest>,
    @JsonProperty("autoupdate")
    val autoUpdate: Boolean = true,
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.JOIN
}