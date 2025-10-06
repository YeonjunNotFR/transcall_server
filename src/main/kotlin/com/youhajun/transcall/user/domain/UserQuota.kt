package com.youhajun.transcall.user.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseEntity
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.dto.RemainTimeResponse
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("user_quotas")
data class UserQuota(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("user_id")
    val userId: UUID?,
    @Column("remaining_seconds")
    val remainingSeconds: Long,
    @Column("reset_at")
    val resetAt: Instant
): BaseUUIDEntity() {
    fun toRemainTimeResponse() = RemainTimeResponse(
        remainingSeconds = remainingSeconds,
        resetAtEpochSeconds = resetAt.epochSecond
    )
}