package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.history.dto.CallHistoryWithParticipantsResponse
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import java.util.*

interface CallHistoryService {
    suspend fun saveCallHistory(userId: UUID, roomId: UUID, title: String): UUID
    suspend fun updateCallHistoryOnLeave(historyId: UUID)

    suspend fun getCallHistory(
        userId: UUID,
        historyId: UUID
    ): CallHistoryResponse

    suspend fun getCallHistoryWithParticipants(
        userId: UUID,
        historyId: UUID
    ): CallHistoryWithParticipantsResponse

    suspend fun getCallHistoriesWithParticipants(
        userId: UUID,
        pagination: CursorPagination
    ): CursorPage<CallHistoryWithParticipantsResponse>
}
