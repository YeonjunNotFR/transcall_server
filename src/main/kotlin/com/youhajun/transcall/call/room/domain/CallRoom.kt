package com.youhajun.transcall.call.room.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import io.r2dbc.spi.Row
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
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
    val janusRoomId: Long,
    @Column("tags")
    val tags: Set<String> = emptySet(),
    @Column("status")
    val status: RoomStatus = RoomStatus.WAITING,
) : BaseUUIDEntity() {

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

    companion object {
        fun from(row: Row): CallRoom = CallRoom(
            uuid = row.get("id", UUID::class.java)!!,
            roomCode = row.get("room_code", String::class.java)!!,
            hostId = row.get("host_id", UUID::class.java),
            title = row.get("title", String::class.java)!!,
            maxParticipants = row.get("max_participants", Int::class.javaObjectType)!!,
            currentParticipantsCount = row.get("current_participants_count", Int::class.javaObjectType)!!,
            visibility = RoomVisibility.fromType(row.get("visibility", String::class.java)!!),
            joinType = RoomJoinType.fromType(row.get("join_type", String::class.java)!!),
            janusRoomId = row.get("janus_room_id", Long::class.javaObjectType)!!,
            tags = (row.get("tags", Array<String>::class.java))?.toSet() ?: emptySet(),
            status = RoomStatus.fromType(row.get("status", String::class.java)!!)
        ).apply {
            createdAt = row.get("created_at", Instant::class.java)!!
            updatedAt = row.get("updated_at", Instant::class.java)!!
        }
    }
}
