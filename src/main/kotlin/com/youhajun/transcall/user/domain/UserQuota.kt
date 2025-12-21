package com.youhajun.transcall.user.domain

import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("user_quotas")
data class UserQuota(
    @Id
    @Column("user_id")
    override val uuid: UUID,
    @Column("remaining_seconds")
    val remainingSeconds: Long,
    @Column("daily_ad_count")
    val dailyAdCount: Int = 0,
    @Column("last_ad_at")
    val lastAdAt: Instant? = null,
    @Column("reset_at")
    val resetAt: Instant,
): BaseUUIDEntity()
