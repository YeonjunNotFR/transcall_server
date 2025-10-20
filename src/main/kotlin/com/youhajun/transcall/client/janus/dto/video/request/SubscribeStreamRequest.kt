package com.youhajun.transcall.client.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscribeStreamRequest(
    @JsonProperty("feed")
    val feedId: Long,
    val mid: String? = null,
    val crossrefid: String? = null
)