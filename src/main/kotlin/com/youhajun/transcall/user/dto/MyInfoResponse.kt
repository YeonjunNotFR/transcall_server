package com.youhajun.transcall.user.dto

import java.util.*

data class MyInfoResponse(
    val userId: UUID,
    val displayName: String,
    val language: String,
    val membershipPlan: String,
    val remainTime: RemainTimeResponse,
    val imageUrl: String,
)