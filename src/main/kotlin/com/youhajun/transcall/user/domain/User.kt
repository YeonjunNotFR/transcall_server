package com.youhajun.transcall.user.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.dto.MyInfoResponse
import com.youhajun.transcall.user.dto.RemainTimeResponse
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("users")
data class User(
    @Id
    @Column("id")
    override val id: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("email")
    val email: String,
    @Column("social_type")
    val socialType: SocialType,
    @Column("nickname")
    val nickname: String,
    @Column("language")
    val language: LanguageType = LanguageType.ENGLISH,
    @Column("membership_plan")
    val membershipPlan: MembershipPlan = MembershipPlan.Free,
    @Column("profile_image_url")
    val profileImageUrl: String? = null,
    @Column("is_active")
    val isActive: Boolean = true,
) : BaseUUIDEntity() {
    fun toMyInfoResponse(remainTime: RemainTimeResponse): MyInfoResponse {
        return MyInfoResponse(
            userId = id,
            displayName = nickname,
            language = language.code,
            membershipPlan = membershipPlan.string,
            imageUrl = profileImageUrl ?: "",
            remainTime = remainTime
        )
    }
}