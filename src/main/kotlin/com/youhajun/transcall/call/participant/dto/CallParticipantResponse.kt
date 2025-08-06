package com.youhajun.transcall.call.participant.dto

import com.youhajun.transcall.user.domain.LanguageType
import java.util.UUID

data class CallParticipantResponse(
    val participantId: UUID,
    val displayName: String,
    val profileImageUrl: String,
    val languageCode: LanguageType,
)