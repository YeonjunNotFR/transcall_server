package com.youhajun.transcall.ws.dto.payload

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.ws.vo.VideoRoomHandleInfo

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