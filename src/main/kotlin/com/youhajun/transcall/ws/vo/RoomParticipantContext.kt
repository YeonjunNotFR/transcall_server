package com.youhajun.transcall.ws.vo

import com.youhajun.transcall.user.domain.LanguageType
import kotlinx.coroutines.Job
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class RoomParticipantContext(
    val userId: UUID,
    val historyId: UUID,
    val participantId: UUID,
    val language: LanguageType,
    val userSession: WebSocketSession,
    val mediaState: MediaStateInfo = MediaStateInfo(),
    val janusSessionInfo: JanusSessionInfo? = null,
    val whisperSessionInfo: WhisperSessionInfo? = null,
    val janusPrivateId: Long? = null
)

data class JanusSessionInfo(
    val session: WebSocketSession,
    val sessionId: Long,
    val handleInfo: VideoRoomHandleInfo,
    val runtimeJob: Job,
    val identity: JanusIdentity? = null,
    val subscriptionMap: ConcurrentHashMap<Long, RemotePublisherInfo> = ConcurrentHashMap(),
)

data class VideoRoomHandleInfo(
    val defaultPublisherHandleId: Long,
    val screenSharePublisherHandleId: Long,
    val subscriberHandleId: Long,
) {
    fun getPubHandleId(mediaContentType: MediaContentType) = when(mediaContentType) {
        MediaContentType.DEFAULT -> defaultPublisherHandleId
        MediaContentType.SCREEN_SHARE -> screenSharePublisherHandleId
    }

    fun identifyHandle(handleId: Long): Pair<Boolean, MediaContentType>? {
        return when (handleId) {
            defaultPublisherHandleId -> true to MediaContentType.DEFAULT
            screenSharePublisherHandleId -> true to MediaContentType.SCREEN_SHARE
            subscriberHandleId -> false to MediaContentType.DEFAULT
            else -> null
        }
    }
}

data class MediaStateInfo(
    val isMicEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true,
)

data class WhisperSessionInfo(
    val whisperSession: WebSocketSession,
    val runtimeJob: Job,
)

data class RemotePublisherInfo(
    val feedId: Long,
    val display: String?,
    val mediaContentType: MediaContentType,
    val videoMid: String? = null,
    val audioMid: String? = null
)

data class JanusIdentity(
    val myFeedId: Long,
    val privateId: Long?
)