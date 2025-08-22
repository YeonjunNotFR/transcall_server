package com.youhajun.transcall.call.participant.dto

import com.youhajun.transcall.user.domain.LanguageType

data class CallParticipantResponse(
    val participantId: String,
    val displayName: String,
    val profileImageUrl: String,
    val language: LanguageType,
)