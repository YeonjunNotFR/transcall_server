package com.youhajun.transcall.ws.dto

import com.youhajun.transcall.janus.dto.video.response.SubscribeStreamResponse

data class SubscriberFeedResponse(
    val type: String,
    val mid: String,
    val feedId: Long,
    val feedMid: String,
    val feedDisplay: String? = null,
    val feedDescription: String? = null,
)

fun SubscribeStreamResponse.toSubscriberFeedResponse() = SubscriberFeedResponse(
    type = type ?: "",
    mid = mid ?: "",
    feedId = feedId ?: 0L,
    feedMid = feedMid ?: "",
    feedDisplay = feedDisplay,
    feedDescription = feedDescription,
)