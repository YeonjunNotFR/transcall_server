package com.youhajun.transcall.call.history.dto

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse

data class CallHistoryResponse(
    val historyId: String,
    val title: String,
    val summary: String,
    val memo: String,
    val isLiked: Boolean,
    val joinedAtToEpochTime: Long,
    val leftAtToEpochTime: Long?,
    val participants: List<CallParticipantResponse>,
) {
    val durationSeconds: Long? = if (leftAtToEpochTime != null) {
        (leftAtToEpochTime - joinedAtToEpochTime) / 1000
    } else {
        null
    }
}