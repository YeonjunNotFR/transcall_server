package com.youhajun.transcall.call.room.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("call_room")
data class CallRoom(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("room_code")
    val roomCode: String,
    @Column("host_id")
    val hostId: UUID?,
    @Column("title")
    val title: String,
    @Column("max_participants")
    val maxParticipants: Int,
    @Column("visibility")
    val visibility: RoomVisibility,
    @Column("tags")
    val tags: Set<String> = emptySet(),
    @Column("status")
    val status: RoomStatus = RoomStatus.WAITING,
) : BaseUUIDEntity()
