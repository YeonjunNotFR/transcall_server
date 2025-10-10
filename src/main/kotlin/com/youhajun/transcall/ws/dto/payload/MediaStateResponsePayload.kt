package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.ws.vo.RoomParticipantSession

sealed interface MediaStateResponse : ResponsePayload

data class MediaStateChanged(
    val mediaState: MediaState,
) : MediaStateResponse {
    companion object {
        const val ACTION = "mediaStateChanged"
    }
}

data class MediaStateInit(
    val mediaStateList: List<MediaState>
) : MediaStateResponse {
    companion object {
        const val ACTION = "mediaStateInit"
    }
}

data class MediaState(
    val userId: String,
    val micEnabled: Boolean,
    val cameraEnabled: Boolean,
)

fun RoomParticipantSession.toMediaStateDto(): MediaState = MediaState(
    userId = userId.toString(),
    micEnabled = mediaState.isMicEnabled,
    cameraEnabled = mediaState.isCameraEnabled
)