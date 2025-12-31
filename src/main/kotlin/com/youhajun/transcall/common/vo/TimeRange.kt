package com.youhajun.transcall.common.vo

import java.time.Instant

data class TimeRange(
    val joinedAt: Instant,
    val leftAt: Instant?,
) {

    companion object {
        fun fromEpochTime(joinedAtEpochMillis: Long, leftAtEpochMillis: Long?): TimeRange = TimeRange(
            joinedAt = Instant.ofEpochMilli(joinedAtEpochMillis),
            leftAt = leftAtEpochMillis?.let { Instant.ofEpochMilli(it) }
        )
    }
}