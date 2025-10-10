package com.youhajun.transcall.ws.dto

import com.youhajun.transcall.client.janus.dto.video.request.SubscribeStreamRequest

data class SubscriberFeedRequest(
    val feedId: Long,
    val mid: String?,
    val crossrefid: String?
) {
    fun toSubscribeStreamRequest() = SubscribeStreamRequest(
        feedId = feedId,
        mid = mid,
        crossrefid = crossrefid
    )
}