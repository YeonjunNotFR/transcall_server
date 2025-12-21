package com.youhajun.transcall.user.domain

import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("user_settings")
data class UserSettings(
    @Id
    @Column("user_id")
    override val uuid: UUID,
    @Column("is_push_enabled")
    val isPushEnabled: Boolean = true,
    @Column("is_marketing_agreed")
    val isMarketingAgreed: Boolean = false,
    @Column("is_privacy_agreed")
    val isPrivacyAgreed: Boolean = true,
    @Column("is_terms_agreed")
    val isTermsAgreed: Boolean = true,
    @Column("privacy_version")
    val privacyVersion: String? = null,
    @Column("marketing_agreed_at")
    val marketingAgreedAt: Instant? = null,
) : BaseUUIDEntity()
