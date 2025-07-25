package com.youhajun.transcall.user.domain

import com.youhajun.transcall.user.dto.RemainTimeResponse
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Table("user_quotas")
data class UserQuota(
    @Id
    @Column("id")
    val id: Long? = null,
    @Column("user_public_id")
    val userPublicId: UUID,
    @Column("remaining_seconds")
    val remainingSeconds: Long,
    @Column("reset_at")
    val resetAt: LocalDateTime
) {
    fun toRemainTimeResponse() = RemainTimeResponse(
        remainingSeconds = remainingSeconds,
        resetAtEpochSeconds = resetAt.toEpochSecond(ZoneOffset.UTC)
    )
}