package com.youhajun.transcall.janus.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscriberStream(
    val feed: Long,
    val mid: String? = null,
    val crossrefid: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StreamUpdate(
    val mid: String,
    val send: Boolean? = null
)

data class JanusIceCandidate(
    val candidate: String,
    val sdpMid: String,
    val sdpMLineIndex: Int
): JanusCandidate

data class JanusTrickleCompleted(
    val completed: Boolean
): JanusCandidate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class StreamInfo(
    val mid: String,
    val type: String,
    val mindex: Int,
    val feedId: Long? = null,
    val codec: String? = null,
    val disabled: Boolean? = false
)

data class PublisherInfo(
    val id: Long,
    val display: String?,
    val streams: List<StreamInfo>?
)

data class Jsep(val type: JSEPType, val sdp: String)

data class JanusError(val code: Int, val reason: String)

data class PluginData<T>(
    val plugin: String,
    val data: T
)
