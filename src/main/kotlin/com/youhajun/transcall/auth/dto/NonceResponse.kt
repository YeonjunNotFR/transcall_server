package com.youhajun.transcall.auth.dto

data class NonceResponse(
    val nonce: String,
    val loginRequestId: String
)