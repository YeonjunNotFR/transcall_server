package com.youhajun.transcall.client.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.client.janus.dto.video.PeerType

data class JoinPublisherRequestBody(
    val ptype: PeerType = PeerType.PUBLISHER,
    @JsonProperty("room")
    val janusRoomId: Long,
    val display: String
) {
    val request: VideoRoomRequestType = VideoRoomRequestType.JOIN
}