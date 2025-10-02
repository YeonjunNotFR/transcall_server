package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import java.util.*

interface CallHistoryService {
    suspend fun getCallHistories(userId: UUID, pagination: CursorPagination): CursorPage<CallHistoryResponse>
    suspend fun saveCallHistory(userId: UUID, roomId: UUID): UUID
    suspend fun updateCallHistoryOnLeave(historyId: UUID)
    suspend fun getCallHistory(userId: UUID, historyId: UUID): CallHistoryResponse
}
