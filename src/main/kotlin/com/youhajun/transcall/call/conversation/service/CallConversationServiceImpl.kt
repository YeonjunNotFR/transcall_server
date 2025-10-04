package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.domain.CallConversation
import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import com.youhajun.transcall.call.conversation.domain.ConversationCache
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.exception.ConversationException
import com.youhajun.transcall.call.conversation.repository.CallConversationRepository
import com.youhajun.transcall.call.conversation.repository.CallConversationTransRepository
import com.youhajun.transcall.call.conversation.repository.ConversationCacheRepository
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import kotlinx.coroutines.flow.Flow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

@Service
class CallConversationServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantService: CallParticipantService,
    private val conversationRepository: CallConversationRepository,
    private val transConversationRepo: CallConversationTransRepository,
    private val conversationCacheRepository: ConversationCacheRepository,
) : CallConversationService {

    companion object {
        private val CONVERSATION_CACHE_TTL = Duration.ofHours(24)
    }

    private val logger: Logger = LogManager.getLogger(CallConversationServiceImpl::class.java)

    override suspend fun publishConversationCache(roomId: UUID, userId: UUID, cache: ConversationCache) {
        transactionalOperator.executeAndAwait {
            val existing = conversationCacheRepository.getConversationCache(roomId, userId)

            val newCache = if (existing != null && existing.start == cache.start) {
                existing.copy(originText = cache.originText)
            } else {
                cache.also { if (existing != null) saveFinalConversation(roomId, userId, existing) }
            }

            conversationCacheRepository.publishCache(roomId, userId, newCache, CONVERSATION_CACHE_TTL)
        }
    }

    override suspend fun subscribeConversationCache(roomId: UUID, userId: UUID): Flow<ConversationCache> {
        return conversationCacheRepository.subscribeCache(roomId, userId)
    }

    private suspend fun saveFinalConversation(roomId: UUID, userId: UUID, cache: ConversationCache): Boolean {
        return transactionalOperator.executeAndAwait {
            try {
                val conversation = CallConversation(
                    uuid = cache.uuid,
                    roomId = roomId,
                    senderId = userId,
                    originText = cache.originText,
                    originLanguage = cache.originLanguage
                )
                conversationRepository.save(conversation)
                true
            } catch (e: DuplicateKeyException) {
                logger.info("Duplicate key exception occurred: ${e.message}")
                false
            }
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
                val conversationList = conversationRepository.findPageByTimeRangeSyncNewest(roomId, timeRange, noneNullCursor, limit, updatedAfterEpochTime)
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

    private suspend fun getConversationsResponse(userId: UUID, conversations: List<CallConversation>): List<ConversationResponse> {
        val conversationIdList = conversations.map { it.id }
        val transList = transConversationRepo.findByConversationIdsAndReceiverId(conversationIdList, userId)
        val transMap = transList.associateBy { it.conversationId }
        return mapToConversationResponses(conversations, transMap)
    }

    private fun mapToConversationResponses(
        conversationList: List<CallConversation>,
        transMap: Map<UUID, CallConversationTrans>
    ): List<ConversationResponse> = conversationList.map { conv ->
        val trans = transMap[conv.id]
        ConversationResponse(
            conversationId = conv.id.toString(),
            roomId = conv.roomId.toString(),
            senderId = conv.senderId.toString(),
            originText = conv.originText,
            originLanguage = conv.originLanguage,
            transText = trans?.translatedText ?: conv.originText,
            transLanguage = trans?.translatedLanguage ?: conv.originLanguage,
            createdAtToEpochTime = conv.createdAt.epochSecond,
            updatedAtToEpochTime = conv.updatedAt.epochSecond,
        )
    }
}
