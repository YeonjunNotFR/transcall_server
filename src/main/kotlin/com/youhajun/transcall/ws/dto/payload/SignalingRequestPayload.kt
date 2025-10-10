package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.ws.dto.SubscriberFeedRequest
import com.youhajun.transcall.ws.vo.MediaContentType

sealed interface SignalingRequest : RequestPayload

data class JoinRoomPublisher(
    val mediaContentType: MediaContentType,
    val handleId: Long,
) : SignalingRequest {
    companion object {
        const val ACTION = "joinPublisher"
    }
}

data class PublisherOffer(
    val offerSdp: String,
    val mediaContentType: MediaContentType,
    val handleId: Long,
    val audioCodec: String?,
    val videoCodec: String?,
    val videoMid: String?,
    val audioMid: String?,
) : SignalingRequest {
    companion object {
        const val ACTION = "publisherOffer"
    }
}

data class JoinRoomSubscriber(
    val privateId: Long,
    val feeds: List<SubscriberFeedRequest>,
) : SignalingRequest {
    companion object {
        const val ACTION = "joinSubscriber"
    }
}

data class SubscriberAnswer(
    val answerSdp: String,
    val handleId: Long
) : SignalingRequest {
    companion object {
        const val ACTION = "subscriberAnswer"
    }
}

data class SubscriberUpdate(
    val subscribeFeeds: List<SubscriberFeedRequest>,
    val unsubscribeFeeds: List<SubscriberFeedRequest>,
) : SignalingRequest {
    companion object {
        const val ACTION = "subscriberUpdate"
    }
}

data class SignalingIceCandidate(
    val handleId: Long,
    val candidate: String,
    val sdpMid: String?,
    val sdpMLineIndex: Int,
) : SignalingRequest {
    companion object {
        const val ACTION = "iceCandidate"
    }
}

data class CompleteIceCandidate(
    val handleId: Long,
) : SignalingRequest {
    companion object {
        const val ACTION = "completeIceCandidate"
    }
}