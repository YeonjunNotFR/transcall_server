package com.youhajun.transcall.common.dto

import java.time.LocalDateTime
import java.time.ZoneOffset

data class ErrorResponse(
    val status: String,
    val message: String,
    val data: Any? = null,
    val timestamp: Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
)