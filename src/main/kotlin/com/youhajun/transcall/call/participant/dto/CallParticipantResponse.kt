package com.youhajun.transcall.call.participant.dto

import com.youhajun.transcall.user.domain.CountryType
import com.youhajun.transcall.user.domain.LanguageType

data class CallParticipantResponse(
    val participantId: String,
    val userId: String,
    val displayName: String,
    val profileImageUrl: String,
    val languageCode: LanguageType,
    val countryCode: CountryType,
    val leftAtToEpochTime: Long?,
)