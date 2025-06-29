package com.youhajun.transcall.common.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val status: String,
    val message: String,
    val data: Any? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
)