package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.history.repository.CallHistoryRepository
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class CallHistoryServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantService: CallParticipantService,
    private val callHistoryRepository: CallHistoryRepository,
) : CallHistoryService {

    override suspend fun getCallHistories(userId: UUID, after: String?, first: Int): CursorPage<CallHistoryResponse> {
        return CursorPaginationHelper.paginate(
            first = first,
            after = after,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                val historyList = callHistoryRepository.findPageByUserIdAndCursor(userId, cursor, limit)
                val roomIds = historyList.map { it.roomId }.distinct()
                val participantsGroup = callParticipantService.findCallParticipantsGroupedByRoomId(roomIds)

                historyList.map { history ->
                    val historyStart = history.joinedAt
                    val historyEnd = history.leftAt ?: LocalDateTime.now()
                    val roomParticipants = participantsGroup[history.roomId] ?: emptyList()
                    val participants = roomParticipants.getTimelineFiltered(historyStart, historyEnd)

                    CallHistoryResponse(
                        historyId = history.id,
                        joinedAtToEpochTime = history.joinedAt.toEpochSecond(ZoneOffset.UTC),
                        leftAtToEpochTime = history.leftAt?.toEpochSecond(ZoneOffset.UTC),
                        participants = participants.map { it.toDto() },
                        title = history.title,
                        summary = history.summary ?: "",
                        memo = history.memo ?: "",
                        isLiked = history.liked
                    )
                }
            },
            convertItemToCursorFunc = {
                UUIDCursor(it.historyId)
            },
        )
    }

    private fun List<CallParticipant>.getTimelineFiltered(
        historyStart: LocalDateTime,
        historyEnd: LocalDateTime
    ): List<CallParticipant> = this.filter { participant ->
        val participantStart = participant.joinedAt
        val participantEnd = participant.leftAt ?: LocalDateTime.now()

        participantStart <= historyEnd && participantEnd >= historyStart
    }
}