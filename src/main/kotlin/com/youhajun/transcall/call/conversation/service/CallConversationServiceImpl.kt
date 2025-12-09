package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.repository.CallConversationRepository
import com.youhajun.transcall.call.conversation.repository.CallConversationTransRepository
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import com.youhajun.transcall.user.domain.LanguageType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class CallConversationServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val conversationRepository: CallConversationRepository,
    private val transConversationRepository: CallConversationTransRepository,
) : CallConversationService {

    private val logger: Logger = LogManager.getLogger(CallConversationServiceImpl::class.java)

    override suspend fun saveConversation(
        roomId: UUID,
        participantId: UUID,
        senderId: UUID,
        originText: String,
        originLanguage: LanguageType
    ): CallConversation {
        return transactionalOperator.executeAndAwait {
            val conversation = CallConversation(
                roomId = roomId,
                participantId = participantId,
                senderId = senderId,
                originText = originText,
                originLanguage = originLanguage
            )
            conversationRepository.save(conversation)
        }
    }

    override suspend fun updateConversationText(conversationId: UUID, text: String) {
        transactionalOperator.executeAndAwait {
            conversationRepository.updateConversationOriginText(conversationId, text)
        }
    }

    override suspend fun getConversationsSyncInTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        updatedAfter: Long
    ): CursorPage<ConversationResponse> {
        return CursorPaginationHelper.paginate(
            cursorPagination = CursorPagination(after = null, first = 0),
            codec = UUIDCursorCodec,
            fetchFunc = { _, _ ->
                conversationRepository.findAllByTimeRangeAndUpdatedAfterOldest(roomId, timeRange, updatedAfter)
            },
            convertItemToCursorFunc = {
                UUIDCursor(UUID.fromString(it.conversationId))
            },
        )
    }

    override suspend fun getConversationsInTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        pagination: CursorPagination,
    ): CursorPage<ConversationResponse> {
        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                conversationRepository.findAllByTimeRangeAndCursorOldest(roomId, timeRange, cursor, limit)
            },
            convertItemToCursorFunc = {
                UUIDCursor(UUID.fromString(it.conversationId))
            },
        )
    }
}
