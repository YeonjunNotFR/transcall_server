package com.youhajun.transcall.client.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class JanusPublisherResponse(
    @JsonProperty("id")
    val feedId: Long,
    val display: String? = null,
    val metadata: Map<String, Any>? = null,
    val streams: List<PublishStreamResponse> = emptyList(),
)