package com.youhajun.transcall.common.dto

import java.time.Instant

data class ErrorResponse(
    val status: String,
    val message: String,
    val data: Any? = null,
    val timestamp: Long = Instant.now().epochSecond
)