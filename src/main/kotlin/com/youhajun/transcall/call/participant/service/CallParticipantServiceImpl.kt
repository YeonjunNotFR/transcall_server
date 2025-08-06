package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.CallException
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.repository.CallParticipantRepository
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.cursor.CreatedAtCursor
import com.youhajun.transcall.pagination.cursor.CreatedAtCursorCodec
import com.youhajun.transcall.user.service.UserService
import org.springframework.stereotype.Service
import java.util.*

@Service
class CallParticipantServiceImpl(
    private val callParticipantRepository: CallParticipantRepository,
    private val userService: UserService
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
                CreatedAtCursor(createdAt = it.createdAt, id = requireNotNull(it.id))
            },
        )
    }

    override suspend fun joinCallParticipant(userPublicId: UUID, roomCode: UUID) {
        val user = userService.findUserByPublicId(userPublicId)
        val participant = CallParticipant(
            roomCode = roomCode,
            userPublicId = userPublicId,
            displayName = user.nickname,
            profileImageUrl = user.profileImageUrl,
            languageType = user.language,
        )

        callParticipantRepository.save(participant)
    }
}