package com.youhajun.transcall.client.janus.dto.event

import com.fasterxml.jackson.annotation.JsonProperty
import com.youhajun.transcall.client.janus.dto.video.response.JanusPublisherResponse
import com.youhajun.transcall.client.janus.dto.video.response.VideoRoomResponseType

data class OnNewPublisher(
    @JsonProperty("videoroom")
    override val videoRoom: VideoRoomResponseType,
    @JsonProperty("room")
    val roomId: String,
    val publishers: List<JanusPublisherResponse> = emptyList()
): VideoRoomEvent