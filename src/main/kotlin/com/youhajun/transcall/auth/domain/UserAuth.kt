package com.youhajun.transcall.auth.domain

import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("user_auth")
data class UserAuth(
    @Id
    @Column("user_id")
    override val uuid: UUID,
    @Column("social_id")
    val socialId: String? = null,
    @Column("refresh_token")
    val refreshToken: String? = null,
    @Column("token_expire_at")
    val tokenExpireAt: Instant? = null,
    @Column("last_login_at")
    val lastLoginAt: Instant? = null,
    @Column("last_login_ip")
    val lastLoginIp: String? = null,
) : BaseUUIDEntity()
