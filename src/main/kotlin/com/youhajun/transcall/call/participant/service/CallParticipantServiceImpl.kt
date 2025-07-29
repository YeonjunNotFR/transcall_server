package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.CallException
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.repository.CallParticipantRepository
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.cursor.CreatedAtCursor
import com.youhajun.transcall.pagination.cursor.CreatedAtCursorCodec
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.*

@Service
class CallParticipantServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callParticipantRepository: CallParticipantRepository
) : CallParticipantService {

    override suspend fun checkCallParticipant(userPublicId: UUID, roomCode: UUID) {
        val roomJoined = callParticipantRepository.existsByRoomCodeAndUserId(roomCode = roomCode, userId = userPublicId)
        if(!roomJoined) throw CallException.ForbiddenCallNotJoin()
    }

    override suspend fun getCallParticipantList(
        userPublicId: UUID,
        after: String?,
        first: Int
    ): CursorPage<CallParticipant> {
        return CursorPaginationHelper.paginate(
            first = first,
            after = after,
            codec = CreatedAtCursorCodec,
            fetchFunc = { cursor, limit ->
                callParticipantRepository.findPageByUserPublicIdAndCursor(
                    userPublicId = userPublicId,
                    cursor = cursor,
                    limit = limit
                )
            },
            convertItemToCursorFunc = {
                CreatedAtCursor(createdAt = it.createdAt, id = it.id!!)
            },
        )
    }
}