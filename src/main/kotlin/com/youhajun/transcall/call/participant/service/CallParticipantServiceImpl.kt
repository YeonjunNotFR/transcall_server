package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.CallException
import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.call.participant.repository.CallParticipantRepository
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class CallParticipantServiceImpl(
    private val callParticipantRepository: CallParticipantRepository,
    private val userService: UserService,
    private val transactionalOperator: TransactionalOperator,
) : CallParticipantService {

    override suspend fun checkParticipant(roomId: UUID, userId: UUID) {
        val roomJoined = callParticipantRepository.existsByRoomIdAndUserId(roomId = roomId, userId = userId)
        if (!roomJoined) throw CallException.ForbiddenCallNotJoin()
    }

    override suspend fun saveParticipant(roomId: UUID, userId: UUID): UUID {
        return transactionalOperator.executeAndAwait {
            val user = userService.findUserById(userId)
            val participant = CallParticipant(
                roomId = roomId,
                userId = userId,
                displayName = user.nickname,
                profileImageUrl = user.profileImageUrl,
                language = user.language,
                country = user.country,
            )

            callParticipantRepository.save(participant).id
        }
    }

    override suspend fun updateParticipantOnLeave(participantId: UUID) {
        transactionalOperator.executeAndAwait {
            callParticipantRepository.updateParticipantOnLeave(participantId)
        }
    }

    override suspend fun findParticipantsGroupByRoomIds(roomIds: List<UUID>): Map<UUID, List<CallParticipant>> {
        return callParticipantRepository.findAllByRoomIds(roomIds).groupBy { it.roomId }
    }

    override suspend fun findCurrentParticipantsGroupByRoomIds(roomIds: List<UUID>): Map<UUID, List<CallParticipant>> {
        return callParticipantRepository.findCurrentParticipantsByRoomIds(roomIds).groupBy { it.roomId }
    }

    override suspend fun findCurrentParticipants(roomId: UUID): List<CallParticipant> {
        return callParticipantRepository.findCurrentParticipantsByRoomId(roomId)
    }

    override suspend fun findParticipantsByRoomIdAndTimeRange(roomId: UUID, timeRange: TimeRange): List<CallParticipant> {
        return callParticipantRepository.findAllByRoomIdAndTimeRange(roomId, timeRange)
    }

    override suspend fun currentCountByRoomId(roomId: UUID): Int {
        return callParticipantRepository.currentCountByRoomId(roomId)
    }
}