package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import java.util.*

interface CallRoomRepositoryCustom {
    suspend fun existsByRoomCode(roomCode: String): Boolean
    suspend fun findByRoomCode(roomCode: String): CallRoom?
    suspend fun findOngoingRoomList(participantCountDirection: SortDirection, createdAtDirection: SortDirection, cursor: UUIDCursor?, limit: Int): List<CallRoom>

    suspend fun updateRoomStatus(roomId: UUID, status: RoomStatus)
    suspend fun updateCurrentParticipantCount(roomId: UUID, count: Int)
}