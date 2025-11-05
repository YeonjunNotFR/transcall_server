package com.youhajun.transcall.ws.dto

import com.youhajun.transcall.janus.dto.video.request.SubscribeStreamBody

data class SubscriberFeedRequest(
    val feedId: Long,
    val mid: String?,
    val crossrefid: String?
) {
    fun toSubscribeStreamRequest() = SubscribeStreamBody(
        feedId = feedId,
        mid = mid ?: "",
        crossrefid = crossrefid ?: ""
    )
}