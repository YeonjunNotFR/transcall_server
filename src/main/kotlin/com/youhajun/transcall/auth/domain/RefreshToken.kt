package com.youhajun.transcall.auth.domain

import com.youhajun.transcall.common.domain.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "refresh_token")
data class RefreshToken(
    @Id
    val id: Long? = null,
    val token: String,
    val userPublicId: String,
    val expireAt: Date
): BaseEntity()