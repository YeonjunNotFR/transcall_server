package com.youhajun.transcall.common.vo

import java.time.Instant

data class TimeRange(
    val joinedAt: Instant,
    val leftAt: Instant?,
) {
    fun isOverlap(otherJoinedAt: Instant, otherLeftAt: Instant?): Boolean {
        val thisLeft = this.leftAt ?: Instant.now()
        val otherLeft = otherLeftAt ?: Instant.now()

        return otherJoinedAt.isBefore(thisLeft) && otherLeft.isAfter(this.joinedAt)
    }

    companion object {
        fun fromEpochTime(joinedAtEpoch: Long, leftAtEpoch: Long?): TimeRange = TimeRange(
            joinedAt = Instant.ofEpochSecond(joinedAtEpoch),
            leftAt = leftAtEpoch?.let { Instant.ofEpochSecond(it) }
        )
    }
}