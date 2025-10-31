package com.youhajun.transcall.ws.vo

import com.youhajun.transcall.user.domain.LanguageType
import kotlinx.coroutines.Job
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

data class RoomParticipantSession(
    val userId: UUID,
    val historyId: UUID,
    val participantId: UUID,
    val language: LanguageType,
    val userSession: WebSocketSession,
    val mediaState: MediaStateInfo = MediaStateInfo(),
    val janusSessionInfo: JanusSessionInfo? = null,
    val whisperSessionInfo: WhisperSessionInfo? = null,
)

data class MediaStateInfo(
    val isMicEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true,
)

data class WhisperSessionInfo(
    val whisperSession: WebSocketSession,
    val runtimeJob: Job,
)

data class JanusSessionInfo(
    val janusSession: WebSocketSession,
    val janusSessionId: Long,
    val videoRoomHandleInfo: VideoRoomHandleInfo,
    val runtimeJob: Job,
    val publisherInfoMap: Map<Long, PublisherInfo> = emptyMap(),
)

data class PublisherInfo(
    val mediaContentType: MediaContentType,
    val feedId: Long,
    val privateId: Long?,
)

data class VideoRoomHandleInfo(
    val defaultPublisherHandleId: Long,
    val screenSharePublisherHandleId: Long,
    val subscriberHandleId: Long,
)