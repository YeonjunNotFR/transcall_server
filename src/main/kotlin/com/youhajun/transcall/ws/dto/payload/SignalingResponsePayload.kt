package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.janus.dto.JanusIceCandidate
import com.youhajun.transcall.janus.dto.PublisherInfo
import com.youhajun.transcall.janus.dto.StreamInfo
import com.youhajun.transcall.ws.vo.MediaContentType

sealed interface SignalingResponse : ResponsePayload

data class PublisherAnswer(
    val answerSdp: String,
    val mediaContentType: MediaContentType
) : SignalingResponse {
    companion object {
        const val ACTION = "publisherAnswer"
    }
}

data class SubscriberOffer(
    val offerSdp: String,
    val feeds: List<StreamInfo>
) : SignalingResponse {
    companion object {
        const val ACTION = "subscriberOffer"
    }
}

data class JoinedEvent(
    val myFeedId: Long,
    val privateId: Long,
    val publishers: List<PublisherInfo>,
    val mediaContentType: MediaContentType
) : SignalingResponse {
    companion object {
        const val ACTION = "joined"
    }
}

data class NewPublisherEvent(
    val mediaContentType: MediaContentType,
    val publishers: List<PublisherInfo>
) : SignalingResponse {
    companion object {
        const val ACTION = "newPublisherEvent"
    }
}

data class PublisherUnpublished(
    val feedId: Long
) : SignalingResponse {
    companion object {
        const val ACTION = "publisherUnpublished"
    }
}
