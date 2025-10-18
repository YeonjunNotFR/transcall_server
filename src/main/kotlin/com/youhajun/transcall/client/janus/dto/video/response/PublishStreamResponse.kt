package com.youhajun.transcall.client.janus.dto.video.response

data class PublishStreamResponse(
    val type: String? = null,
    val mid: String? = null,
    val mindex: Int? = null,
    val description: String? = null,
    val codec: String? = null,
)