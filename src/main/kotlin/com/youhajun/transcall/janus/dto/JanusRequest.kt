package com.youhajun.transcall.janus.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JanusRequest<T>(
    val janus: String,
    val transaction: String,
    @JsonProperty("session_id")
    val sessionId: Long? = null,
    @JsonProperty("handle_id")
    val handleId: Long? = null,
    @JsonProperty("admin_secret")
    val adminSecret: String? = null,
    val plugin: String? = null,
    val body: T? = null,
    val jsep: Jsep? = null,
    val candidate: Any? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "request")
@JsonSubTypes(
    JsonSubTypes.Type(value = VideoRoomBody.JoinPublisher::class, name = "join"),
    JsonSubTypes.Type(value = VideoRoomBody.JoinSubscriber::class, name = "join"),
    JsonSubTypes.Type(value = VideoRoomBody.Configure::class, name = "configure"),
    JsonSubTypes.Type(value = VideoRoomBody.Update::class, name = "update"),
    JsonSubTypes.Type(value = VideoRoomBody.Leave::class, name = "leave"),
    JsonSubTypes.Type(value = VideoRoomBody.Unpublish::class, name = "unpublish"),
    JsonSubTypes.Type(value = VideoRoomBody.Start::class, name = "start"),
    JsonSubTypes.Type(value = VideoRoomBody.CreateRoom::class, name = "create")
)
sealed interface VideoRoomBody {

    val request: String

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class JoinPublisher(
        val room: Long,
        val ptype: PeerType = PeerType.PUBLISHER,
        val display: String? = null,
        val id: Long? = null
    ) : VideoRoomBody {
        override val request: String = "join"
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class JoinSubscriber(
        val room: Long,
        val streams: List<SubscriberStream>,
        val ptype: PeerType = PeerType.SUBSCRIBER,
        val privateId: Long? = null
    ) : VideoRoomBody {
        override val request: String = "join"
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Configure(
        val audio: Boolean? = null,
        val video: Boolean? = null,
        val streams: List<StreamUpdate>? = null
    ) : VideoRoomBody {
        override val request: String = "configure"
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Update(
        val subscribe: List<SubscriberStream>? = null,
        val unsubscribe: List<SubscriberStream>? = null
    ) : VideoRoomBody {
        override val request: String = "update"
    }

    data object Leave : VideoRoomBody {
        override val request: String = "leave"
    }
    data object Unpublish : VideoRoomBody {
        override val request: String = "unpublish"
    }
    data object Start : VideoRoomBody {
        override val request: String = "start"
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class CreateRoom(
        val room: Long? = null,
        val description: String? = null,
        val secret: String? = null,
        val pin: String? = null,
        @JsonProperty("is_private")
        val isPrivate: Boolean? = null,
        val publishers: Int? = null,
        val bitrate: Int? = null,
        val videocodec: String? = "vp8"
    ) : VideoRoomBody {
        override val request: String = "create"
    }
}