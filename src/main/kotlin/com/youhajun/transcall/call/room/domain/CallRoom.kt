package com.youhajun.transcall.call.room.domain

import com.youhajun.transcall.common.domain.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("call_room")
data class CallRoom(
    @Id
    @Column("room_code")
    val roomCode: UUID? = null,
    @Column("host_public_id")
    val hostPublicId: UUID,
    @Column("title")
    val title: String,
    @Column("max_participants")
    val maxParticipants: Int,
    @Column("is_locked")
    val isLocked: Boolean,
    @Column("status")
    val status: RoomStatus,
) : BaseEntity()