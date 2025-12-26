package com.youhajun.transcall.call.participant.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.participant.dto.CallParticipantResponse
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import com.youhajun.transcall.user.domain.CountryType
import com.youhajun.transcall.user.domain.LanguageType
import io.r2dbc.spi.Row
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
    fun toParticipantResponse(): CallParticipantResponse = CallParticipantResponse(
        participantId = id.toString(),
        userId = userId?.toString(),
        roomId = roomId.toString(),
        displayName = displayName,
        profileImageUrl = profileImageUrl ?: "",
        languageCode = language,
        countryCode = country,
        leftAt = leftAt?.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),
        createdAt = createdAt.toEpochMilli(),
    )

    companion object {
        fun from(row: Row, prefix: String = ""): CallParticipant {
            return CallParticipant(
                uuid = row.get("${prefix}id", UUID::class.java)!!,
                roomId = row.get("${prefix}room_id", UUID::class.java)!!,
                userId = row.get("${prefix}user_id", UUID::class.java),
                language = LanguageType.fromCode(row.get("${prefix}language", String::class.java)!!),
                country = CountryType.fromCode(row.get("${prefix}country", String::class.java)!!),
                displayName = row.get("${prefix}display_name", String::class.java)!!,
                profileImageUrl = row.get("${prefix}profile_image_url", String::class.java),
                leftAt = row.get("${prefix}left_at", Instant::class.java)
            ).apply {
                createdAt = row.get("${prefix}created_at", Instant::class.java)!!
                updatedAt = row.get("${prefix}updated_at", Instant::class.java)!!
            }
        }
    }
}