package com.youhajun.transcall.auth.domain

import com.youhajun.transcall.common.domain.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("refresh_token")
data class RefreshToken(
    @Id
    @Column("id")
    val id: Long? = null,
    @Column("token")
    val token: String,
    @Column("user_public_id")
    val userPublicId: UUID,
    @Column("expire_at")
    val expireAt: LocalDateTime
): BaseEntity()