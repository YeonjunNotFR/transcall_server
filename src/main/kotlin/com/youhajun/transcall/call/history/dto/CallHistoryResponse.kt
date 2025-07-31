package com.youhajun.transcall.call.history.dto

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse
import java.util.*

data class CallHistoryResponse(
    val historyId: UUID,
    val title: String,
    val summary: String,
    val memo: String,
    val isLiked: Boolean,
    val startedAtToEpochTime: Long,
    val endedAtToEpochTime: Long,
    val durationSeconds: Int,
    val participants: CallParticipantResponse,
)