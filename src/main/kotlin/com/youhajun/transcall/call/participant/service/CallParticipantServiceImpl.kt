package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.CallException
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.repository.CallParticipantRepository
import com.youhajun.transcall.user.service.UserService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class CallParticipantServiceImpl(
    private val callParticipantRepository: CallParticipantRepository,
    private val userService: UserService
) : CallParticipantService {

    override suspend fun checkCallParticipant(roomId: UUID, userId: UUID) {
        val roomJoined = callParticipantRepository.existsByRoomIdAndUserId(roomId = roomId, userId = userId)
        if (!roomJoined) throw CallException.ForbiddenCallNotJoin()
    }

    override suspend fun joinCallParticipant(roomId: UUID, userId: UUID) {
        val user = userService.findUserById(userId)
        val participant = CallParticipant(
            roomId = roomId,
            userId = userId,
            displayName = user.nickname,
            profileImageUrl = user.profileImageUrl,
            language = user.language,
            joinedAt = LocalDateTime.now()
        )

        callParticipantRepository.save(participant)
    }

    override suspend fun leaveCallParticipant(roomId: UUID, userId: UUID) {
        callParticipantRepository.leaveCallParticipant(roomId, userId)
    }

    override suspend fun findCallParticipantsGroupedByRoomId(roomIds: List<UUID>): Map<UUID, List<CallParticipant>> {
        return callParticipantRepository.findAllByRoomIdIn(roomIds).groupBy { it.roomId }
    }

    override suspend fun findCurrentCallParticipants(roomId: UUID): List<CallParticipant> {
        return callParticipantRepository.findCurrentParticipantsByRoomId(roomId)
    }

    override suspend fun currentCountByRoomId(roomId: UUID): Int {
        return callParticipantRepository.currentCountByRoomId(roomId)
    }
}