package com.youhajun.transcall.common.vo

import java.time.Instant

data class TimeRange(
    val joinedAt: Instant,
    val leftAt: Instant?,
) {

    companion object {
        fun fromEpochTime(joinedAtEpoch: Long, leftAtEpoch: Long?): TimeRange = TimeRange(
            joinedAt = Instant.ofEpochSecond(joinedAtEpoch),
            leftAt = leftAtEpoch?.let { Instant.ofEpochSecond(it) }
        )
    }
}