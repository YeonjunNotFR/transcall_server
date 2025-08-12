package com.youhajun.transcall.call.room.dto

import com.youhajun.transcall.call.room.domain.RoomVisibility
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateRoomRequest(
    val title: String,
    @NotBlank
    val maxParticipantCount: Int,
    @NotBlank
    val visibility: RoomVisibility,
    @Size(min = 0, max = 8)
    val tags: Set<@Max(20) String>,
)