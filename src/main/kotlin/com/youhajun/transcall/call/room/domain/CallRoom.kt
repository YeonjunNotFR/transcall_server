package com.youhajun.transcall.call.room.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.room.dto.OngoingRoomInfoResponse
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
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
    @Column("current_participants_count")
    val currentParticipantsCount: Int,
    @Column("visibility")
    val visibility: RoomVisibility,
    @Column("join_type")
    val joinType: RoomJoinType,
    @Column("janus_room_id")
    val janusRoomId: Long? = null,
    @Column("tags")
    val tags: Set<String> = emptySet(),
    @Column("status")
    val status: RoomStatus = RoomStatus.WAITING,
) : BaseUUIDEntity() {

    fun requireJanusRoomId(): Long = requireNotNull(janusRoomId) {
        "Janus room ID is not set for room with ID: $uuid"
    }

    fun toRoomInfoResponse() = RoomInfoResponse(
        roomId = uuid.toString(),
        roomCode = roomCode,
        title = title,
        maxParticipantCount = maxParticipants,
        currentParticipantCount = currentParticipantsCount,
        visibility = visibility,
        tags = tags,
        status = status,
        joinType = joinType,
        hostId = hostId.toString(),
        createdAtToEpochTime = createdAt.epochSecond
    )

    fun toOngoingRoomInfoResponse(participants: List<CallParticipant>) = OngoingRoomInfoResponse(
        roomInfo = toRoomInfoResponse(),
        currentParticipants = participants.map { it.toDto() }
    )
}
