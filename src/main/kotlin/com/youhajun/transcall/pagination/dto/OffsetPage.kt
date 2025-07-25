package com.youhajun.transcall.pagination.dto

data class OffsetPage<T>(
    val data: List<T> = emptyList(),
    val offset: Int,
    val limit: Int,
    val hasMore: Boolean,
    val totalCount: Long,
)