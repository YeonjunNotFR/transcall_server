package com.youhajun.transcall.janus

import com.youhajun.transcall.janus.dto.JanusIceCandidate
import com.youhajun.transcall.janus.dto.SubscriberStream
import com.youhajun.transcall.janus.dto.VideoRoomBody

class VideoRoomRequestBuilder : JanusRequestBuilder<VideoRoomBody>() {

    private fun setBody(body: VideoRoomBody) = apply { this.body = body }

    fun joinPublisher(room: Long, userId: Long? = null, display: String? = null) =
        setBody(VideoRoomBody.JoinPublisher(room = room, id = userId, display = display))

    fun joinSubscriber(room: Long, streams: List<SubscriberStream>, privateId: Long? = null) =
        setBody(VideoRoomBody.JoinSubscriber(room = room, streams = streams, privateId = privateId))

    fun configure(audio: Boolean? = null, video: Boolean? = null) =
        setBody(VideoRoomBody.Configure(audio = audio, video = video))

    fun start() = setBody(VideoRoomBody.Start)
    fun leave() = setBody(VideoRoomBody.Leave)
    fun unpublish() = setBody(VideoRoomBody.Unpublish)

    fun update(
        subscribe: List<SubscriberStream>? = null,
        unsubscribe: List<SubscriberStream>? = null
    ) = setBody(VideoRoomBody.Update(subscribe = subscribe, unsubscribe = unsubscribe))

    fun trickle(candidate: JanusIceCandidate) = apply {
        this.janus = "trickle"
        this.candidate = candidate
    }

    fun createRoom(
        room: Long? = null,
        description: String? = null,
        secret: String? = null,
        pin: String? = null,
        isPrivate: Boolean? = null,
        publishers: Int? = null,
        bitrate: Int? = null,
        videocodec: String? = null
    ) = setBody(
        VideoRoomBody.CreateRoom(
            room = room,
            description = description,
            secret = secret,
            pin = pin,
            isPrivate = isPrivate,
            publishers = publishers,
            bitrate = bitrate,
            videocodec = videocodec
        )
    )

    fun trickleComplete() = apply {
        this.janus = "trickle"
        this.candidate = mapOf("completed" to true)
    }
}