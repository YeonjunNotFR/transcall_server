package com.youhajun.transcall.janus.dto.video.response

data class StreamInfoResponse(
    val type: String,
    val mindex: String,
    val mid: String,
    val disabled: Boolean = false,
    val codec: String? = null,
    val description: String? = null,
    val moderated: Boolean? = null,
    val simulcast: Boolean? = null,
    val svc: Boolean? = null,
    val talking: Boolean? = null
)