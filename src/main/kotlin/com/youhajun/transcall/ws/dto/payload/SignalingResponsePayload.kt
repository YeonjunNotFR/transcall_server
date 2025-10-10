package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.ws.dto.PublisherFeedResponse
import com.youhajun.transcall.ws.dto.SubscriberFeedResponse
import com.youhajun.transcall.ws.vo.MediaContentType

sealed interface SignalingResponse : ResponsePayload

data class JoinedRoomPublisher(
    val publisherHandleId: Long,
    val mediaContentType: MediaContentType,
    val feeds: List<PublisherFeedResponse>,
    val privateId: Long? = null
) : SignalingResponse {
    companion object {
        const val ACTION = "joinedPublisher"
    }
}

data class NewPublishers(
    val feeds: List<PublisherFeedResponse>,
) : SignalingResponse {
    companion object {
        const val ACTION = "onNewPublisher"
    }
}

data class PublisherAnswer(
    val publisherHandleId: Long,
    val answerSdp: String
) : SignalingResponse {
    companion object {
        const val ACTION = "publisherAnswer"
    }
}

data class SubscriberOffer(
    val subscriberHandleId: Long,
    val offerSdp: String,
    val feeds: List<SubscriberFeedResponse>,
) : SignalingResponse {
    companion object {
        const val ACTION = "subscriberOffer"
    }
}

data class OnIceCandidate(
    val handleId: Long,
    val candidate: String,
    val sdpMid: String?,
    val sdpMLineIndex: Int,
): SignalingResponse {
    companion object {
        const val ACTION = "onIceCandidate"
    }
}