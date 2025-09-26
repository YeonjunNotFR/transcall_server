package com.youhajun.transcall.call.room.dto

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse

data class OngoingRoomInfoResponse(
    val roomInfo: RoomInfoResponse,
    val currentParticipants: List<CallParticipantResponse>,
)