package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.OngoingRoomInfoResponse
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import java.util.*

interface CallRoomService {
    suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID
    suspend fun isRoomFull(roomId: UUID): Boolean
    suspend fun getRoomInfo(roomId: UUID): RoomInfoResponse
    suspend fun getOngoingRoomInfo(roomId: UUID): OngoingRoomInfoResponse
    suspend fun getOngoingRoomList(createdAtSort: SortDirection, participantSort: SortDirection, pagination: CursorPagination): CursorPage<OngoingRoomInfoResponse>
    suspend fun getJanusRoomId(roomId: UUID): Long
    suspend fun joinRoomCheckByCode(userId: UUID, roomCode: String): RoomInfoResponse
    suspend fun updateCurrentParticipantCount(roomId: UUID)
    suspend fun updateRoomStatus(roomId: UUID)
}