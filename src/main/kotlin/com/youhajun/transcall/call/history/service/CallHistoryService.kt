package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.pagination.dto.CursorPage
import java.util.*

interface CallHistoryService {
    suspend fun getCallHistories(userId: UUID, after: String?, first: Int): CursorPage<CallHistoryResponse>
}
