package com.youhajun.transcall.call.conversation.service

import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.repository.CallConversationRepository
import com.youhajun.transcall.call.conversation.repository.CallConversationTransRepository
import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
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
        userId: UUID,
        roomCode: UUID,
        after: String?,
        first: Int
    ): CursorPage<ConversationResponse> {
        callParticipantService.checkCallParticipant(roomCode, userId)

        return CursorPaginationHelper.paginate(
            first = first,
            after = after,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                val conversationList = conversationRepo.findPageByRoomCodeAndCursor(roomCode, cursor, limit)
                val conversationIdList = conversationList.map { it.id }

                val transList = transConversationRepo.findByConversationIdsAndReceiverId(conversationIdList, userId)
                val transMap = transList.associateBy { it.conversationId }

                conversationList.map { conv ->
                    val trans = transMap[conv.id]
                    ConversationResponse(
                        conversationId = conv.id,
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
                UUIDCursor(conversation.conversationId)
            },
        )
    }
}
