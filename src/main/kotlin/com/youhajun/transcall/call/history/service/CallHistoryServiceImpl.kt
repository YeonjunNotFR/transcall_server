package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.domain.CallHistory
import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.history.exception.CallHistoryException
import com.youhajun.transcall.call.history.repository.CallHistoryRepository
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class CallHistoryServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantService: CallParticipantService,
    private val callHistoryRepository: CallHistoryRepository,
    private val callRoomService: CallRoomService,
) : CallHistoryService {

    override suspend fun saveCallHistory(userId: UUID, roomId: UUID): UUID {
        return transactionalOperator.executeAndAwait {
            val roomInfo = callRoomService.getRoomInfo(roomId)
            val history = CallHistory(roomId = roomId, userId = userId, title = roomInfo.title)
            callHistoryRepository.save(history).id
        }
    }

    override suspend fun updateCallHistoryOnLeave(historyId: UUID) {
        transactionalOperator.executeAndAwait {
            callHistoryRepository.updateCallHistoryOnLeave(historyId)
        }
    }

    override suspend fun getCallHistory(userId: UUID, historyId: UUID): CallHistoryResponse {
        val history = callHistoryRepository.findById(historyId) ?: throw CallHistoryException.CallHistoryNotFoundException()
        callParticipantService.checkParticipant(history.roomId, userId)

        val participants = callParticipantService.findParticipantsByRoomIdAndTimeRange(history.roomId, history.toTimeRange())
            .sortedByDescending { it.createdAt }
            .distinctBy { it.userId }
        return history.toHistoryResponse(participants)
    }

    override suspend fun getCallHistories(userId: UUID, pagination: CursorPagination): CursorPage<CallHistoryResponse> {
        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                getCallHistoryResponse(userId, cursor, limit)
            },
            convertItemToCursorFunc = {
                UUIDCursor(UUID.fromString(it.historyId))
            },
        )
    }

    private suspend fun getCallHistoryResponse(
        userId: UUID,
        cursor: UUIDCursor?,
        limit: Int
    ): List<CallHistoryResponse> {
        val historyList = callHistoryRepository.findPageByUserIdAndCursor(userId, cursor, limit)
        val roomIds = historyList.map { it.roomId }.distinct()
        val participantsGroup = callParticipantService.findParticipantsGroupByRoomIds(roomIds)
        return historyList.map { history ->
            val participants = participantsGroup[history.roomId]
                ?.getTimeRangeFiltered(history.toTimeRange())
                ?.sortedByDescending { it.createdAt }
                ?.distinctBy { it.userId }
                ?: emptyList()

            history.toHistoryResponse(participants)
        }
    }

    private fun CallHistory.toTimeRange(): TimeRange = TimeRange(joinedAt = createdAt, leftAt = leftAt)

    private fun List<CallParticipant>.getTimeRangeFiltered(timeRange: TimeRange): List<CallParticipant> =
        filter { timeRange.isOverlap(it.createdAt, it.leftAt) }
}