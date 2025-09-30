package com.youhajun.transcall.call.history.dto

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse

data class CallHistoryResponse(
    val historyId: String,
    val roomId: String,
    val title: String,
    val summary: String,
    val memo: String,
    val isLiked: Boolean,
    val joinedAtToEpochTime: Long,
    val leftAtToEpochTime: Long?,
    val participants: List<CallParticipantResponse>,
) {
    @Suppress("unused")
    val durationSeconds: Long? = if (leftAtToEpochTime != null) {
        (leftAtToEpochTime - joinedAtToEpochTime)
    } else {
        null
    }
}