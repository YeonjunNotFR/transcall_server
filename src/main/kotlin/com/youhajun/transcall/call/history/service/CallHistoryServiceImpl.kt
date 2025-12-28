package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.domain.CallHistory
import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.history.dto.CallHistoryWithParticipantsResponse
import com.youhajun.transcall.call.history.exception.CallHistoryException
import com.youhajun.transcall.call.history.repository.CallHistoryRepository
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import com.youhajun.transcall.pagination.vo.PagingDirection
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class CallHistoryServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callHistoryRepository: CallHistoryRepository,
) : CallHistoryService {

    override suspend fun saveCallHistory(userId: UUID, roomId: UUID, title: String): UUID {
        return transactionalOperator.executeAndAwait {
            val history = CallHistory(roomId = roomId, userId = userId, title = title)
            callHistoryRepository.save(history).id
        }
    }

    override suspend fun updateCallHistoryOnLeave(historyId: UUID) {
        transactionalOperator.executeAndAwait {
            callHistoryRepository.updateCallHistoryOnLeave(historyId)
        }
    }

    override suspend fun getCallHistory(userId: UUID, historyId: UUID): CallHistoryResponse {
        return callHistoryRepository.findById(historyId)?.toHistoryResponse() ?: throw CallHistoryException.CallHistoryNotFoundException()
    }

    override suspend fun getCallHistoryWithParticipants(
        userId: UUID,
        historyId: UUID
    ): CallHistoryWithParticipantsResponse {
        return callHistoryRepository.findCallHistoryWithParticipants(historyId = historyId, userId = userId) ?: throw CallHistoryException.CallHistoryNotFoundException()
    }

    override suspend fun getCallHistoriesWithParticipants(
        userId: UUID,
        pagination: CursorPagination,
        direction: PagingDirection
    ): CursorPage<CallHistoryWithParticipantsResponse> {
        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                callHistoryRepository.findAllCallHistoryWithParticipants(userId, cursor, limit, direction)
            },
            convertItemToCursorFunc = {
                UUIDCursor(UUID.fromString(it.history.historyId))
            },
        )
    }
}