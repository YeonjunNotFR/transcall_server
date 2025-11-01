package com.youhajun.transcall.call.participant.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.participant.dto.CallParticipantResponse
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.domain.CountryType
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("call_participant")
data class CallParticipant(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("room_id")
    val roomId: UUID,
    @Column("user_id")
    val userId: UUID?,
    @Column("language")
    val language: LanguageType,
    @Column("country")
    val country: CountryType,
    @Column("display_name")
    val displayName: String,
    @Column("profile_image_url")
    val profileImageUrl: String?,
    @Column("left_at")
    val leftAt: Instant? = null,
) : BaseUUIDEntity() {
    fun toDto(): CallParticipantResponse = CallParticipantResponse(
        participantId = id.toString(),
        userId = userId.toString(),
        displayName = displayName,
        profileImageUrl = profileImageUrl ?: "",
        languageCode = language,
        countryCode = country,
        leftAtToEpochTime = leftAt?.epochSecond
    )
}