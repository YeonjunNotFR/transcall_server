package com.youhajun.transcall.auth.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("refresh_token")
data class RefreshToken(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("token")
    val token: String,
    @Column("user_id")
    val userId: UUID?,
    @Column("expire_at")
    val expireAt: Instant
): BaseUUIDEntity()
