package com.youhajun.transcall.call.history.dto

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse

data class CallHistoryWithParticipantsResponse(
    val history: CallHistoryResponse,
    val participants: List<CallParticipantResponse>
)