package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.janus.dto.JanusIceCandidate
import com.youhajun.transcall.janus.dto.SubscriberStream
import com.youhajun.transcall.ws.vo.MediaContentType

sealed interface SignalingRequest : RequestPayload

data class JoinPublisher(
    val mediaContentType: MediaContentType,
) : SignalingRequest {
    companion object {
        const val ACTION = "joinPublisher"
    }
}

data class JoinSubscriber(
    val privateId: Long?,
    val subscribeFeeds: List<SubscriberStream>
) : SignalingRequest {
    companion object {
        const val ACTION = "joinSubscriber"
    }
}

data class PublisherOffer(
    val offerSdp: String,
    val mediaContentType: MediaContentType,
) : SignalingRequest {
    companion object {
        const val ACTION = "publisherOffer"
    }
}

data class SubscriberAnswer(
    val answerSdp: String,
) : SignalingRequest {
    companion object {
        const val ACTION = "subscriberAnswer"
    }
}

data class SubscriberUpdate(
    val subscribeFeeds: List<SubscriberStream>,
    val unsubscribeFeeds: List<SubscriberStream>,
) : SignalingRequest {
    companion object {
        const val ACTION = "subscriberUpdate"
    }
}

data class SignalingIceCandidate(
    val mediaContentType: MediaContentType,
    val isPublisher: Boolean,
    val janusCandidate: JanusIceCandidate,
) : SignalingRequest, SignalingResponse {
    companion object {
        const val ACTION = "iceCandidate"
    }
}

data class IceCandidateComplete(
    val mediaContentType: MediaContentType,
    val isPublisher: Boolean,
) : SignalingRequest {
    companion object {
        const val ACTION = "iceCandidateComplete"
    }
}