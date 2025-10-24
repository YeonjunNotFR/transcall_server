package com.youhajun.transcall.ws.dto

import com.youhajun.transcall.janus.dto.video.response.JanusPublisherResponse

data class PublisherFeedResponse(
    val feedId: Long,
    val display: String,
    val streams: List<PublisherFeedResponseStream>
)

data class PublisherFeedResponseStream(
    val type: String,
    val mid: String,
)

fun JanusPublisherResponse.toPublisherFeedResponse(): PublisherFeedResponse = PublisherFeedResponse(
    feedId = feedId,
    display = display ?: "",
    streams = streams.map { stream ->
        PublisherFeedResponseStream(
            type = stream.type ?: "",
            mid = stream.mid ?: "",
        )
    }
)