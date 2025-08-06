package com.youhajun.transcall.call.participant.domain

import com.youhajun.transcall.call.participant.dto.CallParticipantResponse
import com.youhajun.transcall.common.domain.BaseEntity
import com.youhajun.transcall.user.domain.LanguageType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("call_participant")
data class CallParticipant(
    @Id
    @Column("id")
    val id: UUID? = null,
    @Column("room_code")
    val roomCode: UUID,
    @Column("user_public_id")
    val userPublicId: UUID,
    @Column("language")
    val languageType: LanguageType,
    @Column("display_name")
    val displayName: String,
    @Column("profile_image_url")
    val profileImageUrl: String?,
    @Column("history_title")
    val historyTitle: String = "",
    @Column("history_summary")
    val historySummary: String = "",
    @Column("history_memo")
    val historyMemo: String = "",
    @Column("history_liked")
    val historyLiked: Boolean = false,
    @Column("history_deleted")
    val historyDeleted: Boolean = false,
) : BaseEntity() {
    fun toDto(): CallParticipantResponse = CallParticipantResponse(
        participantId = requireNotNull(id),
        displayName = displayName,
        profileImageUrl = profileImageUrl ?: "",
        languageCode = languageType,
    )
}