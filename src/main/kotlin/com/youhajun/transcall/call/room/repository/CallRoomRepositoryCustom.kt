package com.youhajun.transcall.call.room.repository

import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.dto.RoomInfoWithParticipantsResponse
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import java.util.*

interface CallRoomRepositoryCustom {
    suspend fun existsByRoomCode(roomCode: String): Boolean
    suspend fun findByRoomCode(roomCode: String): CallRoom?
    suspend fun findAllCurrentRoomInfoWithParticipants(
        participantSort: SortDirection,
        createdAtSort: SortDirection,
        cursor: UUIDCursor?, limit: Int
    ): List<RoomInfoWithParticipantsResponse>

    suspend fun updateRoomStatusAndCount(roomId: UUID)
}