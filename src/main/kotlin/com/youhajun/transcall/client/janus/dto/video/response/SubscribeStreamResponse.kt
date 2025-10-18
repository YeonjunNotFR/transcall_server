package com.youhajun.transcall.client.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonProperty

data class SubscribeStreamResponse(
    val type: String? = null,
    val active: Boolean? = null,
    val mindex: Int? = null,
    val mid: String? = null,
    val crossrefid: String? = null,
    val ready: Boolean? = null,
    val send: Boolean? = null,
    @JsonProperty("feed_id")
    val feedId: Long? = null,
    @JsonProperty("feed_display")
    val feedDisplay: String? = null,
    @JsonProperty("feed_mid")
    val feedMid: String? = null,
    @JsonProperty("feed_description")
    val feedDescription: String? = null,
    val codec: String? = null,
)