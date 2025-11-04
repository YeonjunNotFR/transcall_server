package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.exception.ConversationException
import com.youhajun.transcall.call.conversation.repository.CallConversationRepository
import com.youhajun.transcall.call.conversation.repository.CallConversationTransRepository
import com.youhajun.transcall.call.participant.service.CallParticipantService
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
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class CallConversationServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantService: CallParticipantService,
    private val conversationRepository: CallConversationRepository,
    private val transConversationRepository: CallConversationTransRepository,
) : CallConversationService {

    private val logger: Logger = LogManager.getLogger(CallConversationServiceImpl::class.java)

    override suspend fun saveConversation(
        roomId: UUID,
        senderId: UUID,
        originText: String,
        originLanguage: LanguageType
    ): CallConversation {
        return transactionalOperator.executeAndAwait {
            val conversation = CallConversation(
                roomId = roomId,
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

    override suspend fun getConversationsSyncTimeRange(
        userId: UUID,
        roomId: UUID,
        timeRange: TimeRange,
        pagination: CursorPagination,
        updatedAfter: Long?
    ): CursorPage<ConversationResponse> {
        callParticipantService.checkParticipant(roomId = roomId, userId = userId)
        val updatedAfterEpochTime = updatedAfter ?: Instant.now().epochSecond

        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                val noneNullCursor = cursor ?: throw ConversationException.ConversationSyncNeedAfter()
                val conversationList = conversationRepository.findPageByTimeRangeSyncNewest(
                    roomId,
                    timeRange,
                    noneNullCursor,
                    limit,
                    updatedAfterEpochTime
                )
                getConversationsResponse(userId, conversationList)
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
        callParticipantService.checkParticipant(roomId = roomId, userId = userId)

        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                val conversationList = conversationRepository.findPageByTimeRangeAndCursorOldest(roomId, timeRange, cursor, limit)
                getConversationsResponse(userId, conversationList)
            },
            convertItemToCursorFunc = {
                UUIDCursor(UUID.fromString(it.conversationId))
            },
        )
    }

    private suspend fun getConversationsResponse(
        userId: UUID,
        conversations: List<CallConversation>
    ): List<ConversationResponse> {
        val conversationIdList = conversations.map { it.id }
        val targetLanguage = LanguageType.ENGLISH
        val transList = transConversationRepository.findByConversationIdsAndLanguage(conversationIdList, targetLanguage)
        val transMap = transList.associateBy { it.conversationId }
        return mapToConversationResponses(conversations, transMap)
    }

    private fun mapToConversationResponses(
        conversationList: List<CallConversation>,
        transMap: Map<UUID, CallConversationTrans>
    ): List<ConversationResponse> = conversationList.map { conv ->
        val trans = transMap[conv.id]
        conv.toConversationResponse(
            transText = trans?.translatedText,
            transLanguage = trans?.translatedLanguage,
        )
    }
}
