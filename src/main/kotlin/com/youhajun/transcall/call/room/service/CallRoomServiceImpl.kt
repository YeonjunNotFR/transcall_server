package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomJoinType
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.call.room.exception.RoomException
import com.youhajun.transcall.call.room.repository.CallRoomRepository
import com.youhajun.transcall.call.room.repository.JanusRoomIdCacheRepository
import com.youhajun.transcall.janus.dto.video.request.CreateVideoRoomRequest
import com.youhajun.transcall.janus.service.JanusVideoRoomService
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Duration
import java.util.*

@Service
class CallRoomServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val callRoomRepository: CallRoomRepository,
    private val janusRoomIdCacheRepository: JanusRoomIdCacheRepository,
    private val callParticipantService: CallParticipantService,
    private val janusVideoRoomService: JanusVideoRoomService
) : CallRoomService {

    companion object {
        private val JANUS_ROOM_ID_CACHE_TTL = Duration.ofHours(24)
        private const val ROOM_CODE_LENGTH = 8
    }

    override suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID {
        return transactionalOperator.executeAndAwait {
            val room = CallRoom(
                hostId = userId,
                title = request.title,
                maxParticipants = request.maxParticipantCount,
                visibility = request.visibility,
                tags = request.tags,
                roomCode = generateUniqueRoomCode(),
                joinType = RoomJoinType.CODE_JOIN,
            )

            val saved = callRoomRepository.save(room)
            val reloaded = callRoomRepository.findById(saved.id) ?: throw RoomException.RoomNotFound()

            val janusRoomId = reloaded.requireJanusRoomId()
            val janusRequest = CreateVideoRoomRequest(janusRoomId)
            janusVideoRoomService.createRoom(janusRequest).getOrThrow()
            janusRoomIdCacheRepository.saveJanusRoomId(reloaded.id, janusRoomId, JANUS_ROOM_ID_CACHE_TTL)
            reloaded.id
        }
    }

    override suspend fun joinRoomByCode(userId: UUID, roomCode: String): RoomInfoResponse {
        val room = callRoomRepository.findByRoomCode(roomCode) ?: throw RoomException.RoomNotFound()
        if(isRoomFull(room.id)) throw RoomException.RoomIsFull()

        return room.toDto()
    }

    override suspend fun isRoomFull(roomId: UUID): Boolean {
        val room = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
        val participantCount = callParticipantService.currentCountByRoomId(roomId)
        return participantCount >= room.maxParticipants
    }

    override suspend fun getRoomInfo(roomId: UUID): RoomInfoResponse {
        return callRoomRepository.findById(roomId)?.toDto() ?: throw RoomException.RoomNotFound()
    }

    override suspend fun getJanusRoomId(roomId: UUID): Long {
        janusRoomIdCacheRepository.getJanusRoomId(roomId)?.let { cachedId ->
            return cachedId
        }

        val room = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
        val janusRoomId = room.requireJanusRoomId()
        janusRoomIdCacheRepository.saveJanusRoomId(roomId, janusRoomId, JANUS_ROOM_ID_CACHE_TTL)
        return janusRoomId
    }

    override suspend fun updateRoomStatus(roomId: UUID): Boolean {
        val count = callParticipantService.currentCountByRoomId(roomId)
        val status = when(count) {
            0 -> RoomStatus.ENDED
            1 -> RoomStatus.WAITING
            else -> RoomStatus.IN_PROGRESS
        }
        return callRoomRepository.updateRoomStatus(roomId, status)
    }

    private suspend fun generateUniqueRoomCode(): String {
        repeat(10) {
            val code = generateRoomCode()
            val exists = callRoomRepository.existsByRoomCode(code)
            if (!exists) return code
        }
        throw RoomException.RoomCodeGenerationFailed()
    }

    private fun generateRoomCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..ROOM_CODE_LENGTH)
            .map { chars.random() }
            .joinToString("")
    }
}