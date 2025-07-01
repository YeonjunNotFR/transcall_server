package com.youhajun.transcall.user.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("remain_time")
data class RemainTime(
    @Id
    val id: Long,
    val remainingSeconds: Int,
    val resetAtEpochSeconds: Long,
    val dailyLimitSeconds: Int? = null
)