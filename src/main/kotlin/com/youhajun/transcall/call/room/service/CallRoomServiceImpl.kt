package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.exception.RoomException
import com.youhajun.transcall.call.room.repository.CallRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class CallRoomServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callRoomRepository: CallRoomRepository,
    private val callParticipantService: CallParticipantService,
) : CallRoomService {

    override suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID {
        return transactionalOperator.executeAndAwait {
            val room = CallRoom(
                hostId = userId,
                title = request.title,
                maxParticipants = request.maxParticipantCount,
                visibility = request.visibility,
                tags = request.tags,
                roomCode = generateUniqueRoomCode()
            )

            val savedRoom = callRoomRepository.save(room)
            callParticipantService.joinCallParticipant(userId, savedRoom.id)
            savedRoom.id
        }
    }

    private suspend fun generateUniqueRoomCode(): String {
        repeat(10) {
            val code = generateRoomCode(length = 8)
            val exists = callRoomRepository.existsByRoomCode(code)
            if (!exists) return code
        }
        throw RoomException.RoomCodeGenerationFailed()
    }

    private fun generateRoomCode(length: Int): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}