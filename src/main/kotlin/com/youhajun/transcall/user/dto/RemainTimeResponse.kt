package com.youhajun.transcall.user.dto

data class RemainTimeResponse(
    val remainingSeconds: Long = 0,
    val resetAtEpochSeconds: Long = 0,
)