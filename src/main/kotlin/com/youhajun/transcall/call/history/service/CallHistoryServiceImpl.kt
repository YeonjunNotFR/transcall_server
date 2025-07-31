package com.youhajun.transcall.call.history.service

import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.history.exception.CallHistoryException
import com.youhajun.transcall.call.history.repository.CallHistoryRepository
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.dto.Node
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.ZoneOffset
import java.util.*

@Service
class CallHistoryServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantService: CallParticipantService,
    private val callHistoryRepository: CallHistoryRepository,
) : CallHistoryService {

    override suspend fun getCallHistories(userPublicId: UUID, after: String?, first: Int): CursorPage<CallHistoryResponse> {
        val participantPage = callParticipantService.getCallParticipantList(userPublicId, after, first)
        val roomCodes = participantPage.edges.map { it.node.roomCode }.distinct()
        val histories = callHistoryRepository.findByRoomCodeIn(roomCodes)
        val historyMap = histories.associateBy { it.roomCode }

        val historyEdges = participantPage.edges.map { edge ->
            val participant = edge.node
            val history = historyMap[participant.roomCode] ?: throw CallHistoryException.CallHistoryNotFoundException()

            Node(
                node = CallHistoryResponse(
                    historyId = history.id!!,
                    startedAtToEpochTime = history.startedAt.toEpochSecond(ZoneOffset.UTC),
                    endedAtToEpochTime = history.endedAt.toEpochSecond(ZoneOffset.UTC),
                    durationSeconds = history.durationSeconds,
                    participants = participant.toDto(),
                    title = participant.historyTitle,
                    summary = participant.historySummary,
                    memo = participant.historyMemo,
                    isLiked = participant.historyLiked
                ),
                cursor = edge.cursor
            )
        }

        return CursorPage(
            edges = historyEdges,
            pageInfo = participantPage.pageInfo,
            totalCount = participantPage.totalCount
        )
    }
}