package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.repository.CallConversationRepository
import com.youhajun.transcall.call.conversation.repository.CallConversationTransRepository
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.pagination.cursor.CreatedAtCursor
import com.youhajun.transcall.pagination.cursor.CreatedAtCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.CursorPaginationHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class CallConversationServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantService: CallParticipantService,
    private val conversationRepo: CallConversationRepository,
    private val transConversationRepo: CallConversationTransRepository
) : CallConversationService {

    override suspend fun getCallConversations(
        userPublicId: UUID,
        roomCode: UUID,
        after: String?,
        first: Int
    ): CursorPage<ConversationResponse> {
        callParticipantService.checkCallParticipant(roomCode, userPublicId)

        return CursorPaginationHelper.paginate(
            first = first,
            after = after,
            codec = CreatedAtCursorCodec,
            fetchFunc = { cursor, limit ->
                val conversationList = conversationRepo.findPageByRoomCodeAndCursor(roomCode, cursor, limit)
                val conversationIdList = conversationList.mapNotNull { it.id }

                val transList = transConversationRepo.findByConversationIdsAndReceiverId(conversationIdList, userPublicId)
                val transMap = transList.associateBy { it.conversationId }

                conversationList.map { conv ->
                    val convId = requireNotNull(conv.id)
                    val trans = transMap[convId]
                    ConversationResponse(
                        conversationId = convId,
                        senderId = conv.senderId,
                        originText = conv.originText,
                        originLanguage = conv.originLanguage,
                        translatedText = trans?.translatedText ?: conv.originText,
                        translatedLanguage = trans?.translatedLanguage ?: conv.originLanguage,
                        createdAtToEpochTime = conv.createdAt.toEpochSecond(ZoneOffset.UTC)
                    )
                }
            },
            convertItemToCursorFunc = { conversation ->
                val createdAt = LocalDateTime.ofEpochSecond(conversation.createdAtToEpochTime, 0, ZoneOffset.UTC)
                CreatedAtCursor(id = conversation.conversationId, createdAt = createdAt)
            },
        )
    }
}
