package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse
import com.youhajun.transcall.call.room.dto.RoomInfoResponse

sealed interface RoomResponsePayload : ResponsePayload

data class ConnectedRoom(
    val roomInfo: RoomInfoResponse,
    val participants: List<CallParticipantResponse>,
) : RoomResponsePayload {
    companion object {
        const val ACTION = "connected"
    }
}

data class ChangedRoom(
    val roomInfo: RoomInfoResponse,
    val participants: List<CallParticipantResponse>,
) : RoomResponsePayload {
    companion object {
        const val ACTION = "changedRoom"
    }
}