package com.youhajun.transcall.janus.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "janus",
    visible = true,
    defaultImpl = JanusGenericResponse::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = JanusSuccessResponse::class, name = "success"),
    JsonSubTypes.Type(value = JanusEventResponse::class, name = "event"),
    JsonSubTypes.Type(value = JanusErrorResponse::class, name = "error"),
    JsonSubTypes.Type(value = JanusAckResponse::class, name = "ack"),
    JsonSubTypes.Type(value = JanusTrickleResponse::class, name = "trickle"),
    JsonSubTypes.Type(value = JanusMediaResponse::class, name = "media")
)
sealed interface JanusResponse {
    val janus: String
    val transaction: String?
    val sessionId: Long?
    val sender: Long?
}

data class JanusSuccessResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?,
    val data: JanusData? = null,
    val plugindata: PluginData<VideoRoomData>?,
    val jsep: Jsep?
) : JanusResponse

data class JanusData(
    val id: Long
)

data class JanusEventResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?,
    val plugindata: PluginData<VideoRoomData>?,
    val jsep: Jsep?
) : JanusResponse

data class JanusAckResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?
) : JanusResponse

data class JanusErrorResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?,
    val error: JanusError
) : JanusResponse

data class JanusGenericResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?
) : JanusResponse

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(value = JanusIceCandidate::class),
    JsonSubTypes.Type(value = JanusTrickleCompleted::class)
)
sealed interface JanusCandidate

data class JanusTrickleResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?,
    val candidate: JanusCandidate
) : JanusResponse

data class JanusMediaResponse(
    override val janus: String,
    override val transaction: String?,
    override val sessionId: Long?,
    override val sender: Long?,
    val type: String,
    val receiving: Boolean
) : JanusResponse

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "videoroom",
    visible = true,
    defaultImpl = VideoRoomData.Unknown::class
)
@JsonSubTypes(
    JsonSubTypes.Type(value = VideoRoomData.Created::class, name = "created"),
    JsonSubTypes.Type(value = VideoRoomData.Joined::class, name = "joined"),
    JsonSubTypes.Type(value = VideoRoomData.Event::class, name = "event"),
    JsonSubTypes.Type(value = VideoRoomData.Destroyed::class, name = "destroyed"),
    JsonSubTypes.Type(value = VideoRoomData.Attached::class, name = "attached"),

)
sealed interface VideoRoomData {

    data class Created(
        val videoroom: String = "created",
        val room: Long?,
    ) : VideoRoomData

    data class Joined(
        val videoroom: String = "joined",
        val id: Long,
        val privateId: Long?,
        val publishers: List<PublisherInfo>?
    ) : VideoRoomData

    data class Event(
        val videoroom: String = "event",
        val room: Long?,
        val publishers: List<PublisherInfo>? = null,
        val streams: List<StreamInfo>? = null,
        val leaving: Any? = null,
        val unpublished: Long? = null,
        val configured: String? = null,
        val error: String? = null,
    ) : VideoRoomData

    data class Destroyed(
        val videoroom: String = "destroyed",
        val room: Long
    ) : VideoRoomData

    data class Attached(
        val videoroom: String = "attached",
        val room: Long?,
        val streams: List<StreamInfo>? = null
    ) : VideoRoomData

    data class Unknown(
        val videoroom: String? = null,
        val error: String? = null,
        val error_code: Int? = null
    ) : VideoRoomData
}

data class JanusJoinPublisherResponse(
    val myFeedId: Long,
    val myPrivateId: Long,
    val publishers: List<PublisherInfo>
)

data class JanusPublisherAnswerResponse(
    val answerSdp: String
)

data class JanusSubscriberOfferResponse(
    val offerSdp: String,
    val feeds: List<StreamInfo>
)

object JanusActionSuccess

inline fun <reified T : VideoRoomData> JanusResponse.requireData(): T {
    val plugindata = when (this) {
        is JanusSuccessResponse -> this.plugindata
        is JanusEventResponse -> this.plugindata
        else -> null
    }

    val data = plugindata?.data
    return data as? T ?: throw IllegalStateException("기대하는 데이터 타입(${T::class.simpleName})이 아닙니다. (Janus: $janus)")
}

fun JanusResponse.requireJsep(): Jsep {
    val jsep = when (this) {
        is JanusSuccessResponse -> this.jsep
        is JanusEventResponse -> this.jsep
        else -> null
    }
    return jsep ?: throw IllegalStateException("응답에 JSEP(SDP) 정보가 없습니다. (Janus: $janus)")
}