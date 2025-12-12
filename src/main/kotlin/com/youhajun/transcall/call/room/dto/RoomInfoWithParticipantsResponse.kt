package com.youhajun.transcall.call.room.dto

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse

data class RoomInfoWithParticipantsResponse(
    val roomInfo: RoomInfoResponse,
    val participants: List<CallParticipantResponse>
)