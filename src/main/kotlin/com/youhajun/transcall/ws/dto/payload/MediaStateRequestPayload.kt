package com.youhajun.transcall.ws.dto.payload

sealed interface MediaStateRequest : RequestPayload

data class CameraEnableChanged(
    val isEnabled: Boolean
) : MediaStateRequest {
    companion object {
        const val ACTION = "cameraEnableChanged"
    }
}

data class MicEnableChanged(
    val isEnabled: Boolean
) : MediaStateRequest {
    companion object {
        const val ACTION = "micEnableChanged"
    }
}