package com.youhajun.transcall.call.room.dto

import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.domain.RoomVisibility
import java.util.UUID

data class RoomInfoResponse(
    val roomId: UUID,
    val roomCode: String,
    val title: String,
    val maxParticipantCount: Int,
    val visibility: RoomVisibility,
    val tags: Set<String>,
    val status: RoomStatus,
)