package com.youhajun.transcall.call.history.dto

data class CallHistoryResponse(
    val historyId: String,
    val roomId: String,
    val title: String,
    val summary: String,
    val memo: String,
    val isLiked: Boolean,
    val leftAt: Long?,
    val updatedAt: Long,
    val createdAt: Long,
)