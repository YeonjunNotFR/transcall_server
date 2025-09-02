package com.youhajun.transcall.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.janus.dto.video.PeerType

data class JoinSubscriberRequestBody(
    val ptype: PeerType = PeerType.SUBSCRIBER,
    @JsonProperty("room")
    val janusRoomId: Long,
    @JsonProperty("private_id")
    val privateId: Long?,
    val streams: List<StreamInfoRequest>,
    @JsonProperty("autoupdate")
    val autoUpdate: Boolean = true,
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.JOIN
}