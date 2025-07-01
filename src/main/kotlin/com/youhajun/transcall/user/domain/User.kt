package com.youhajun.transcall.user.domain

import com.youhajun.transcall.common.domain.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("users")
data class User(
    @Id
    val id: Long? = null,
    val publicId: UUID = UUID.randomUUID(),
    val email: String,
    val socialType: SocialType,
    val nickname: String,
    val language: LanguageType = LanguageType.ENGLISH,
    val membershipPlan: MembershipPlan = MembershipPlan.Free,
    val profileImageUrl: String? = null,
    val isActive: Boolean = true,
) : BaseEntity()