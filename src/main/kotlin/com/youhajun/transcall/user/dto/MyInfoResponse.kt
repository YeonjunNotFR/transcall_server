package com.youhajun.transcall.user.dto

import com.youhajun.transcall.user.domain.CountryType
import com.youhajun.transcall.user.domain.LanguageType

data class MyInfoResponse(
    val userId: String,
    val displayName: String,
    val languageCode: LanguageType,
    val countryCode: CountryType,
    val membershipPlan: String,
    val remainTime: RemainTimeResponse,
    val imageUrl: String,
)