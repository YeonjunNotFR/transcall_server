package com.youhajun.transcall.call.room.dto

import com.youhajun.transcall.call.room.domain.RoomJoinType
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.domain.RoomVisibility

data class RoomInfoResponse(
    val roomId: String,
    val roomCode: String,
    val title: String,
    val maxParticipantCount: Int,
    val currentParticipantCount: Int,
    val visibility: RoomVisibility,
    val joinType: RoomJoinType,
    val tags: Set<String>,
    val status: RoomStatus,
    val hostId: String,
    val createdAt: Long
)