package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomJoinType
import com.youhajun.transcall.call.room.domain.RoomStatus
import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.OngoingRoomInfoResponse
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.call.room.exception.RoomException
import com.youhajun.transcall.call.room.repository.CallRoomRepository
import com.youhajun.transcall.call.room.repository.JanusRoomIdCacheRepository
import com.youhajun.transcall.client.janus.dto.video.request.CreateVideoRoomRequest
import com.youhajun.transcall.client.janus.service.JanusVideoRoomService
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.CursorPaginationHelper
import com.youhajun.transcall.pagination.cursor.UUIDCursor
import com.youhajun.transcall.pagination.cursor.UUIDCursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
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
    private val janusVideoRoomService: JanusVideoRoomService,
) : CallRoomService {

    companion object {
        private val JANUS_ROOM_ID_CACHE_TTL = Duration.ofHours(24)
        private const val ROOM_CODE_LENGTH = 5
    }

    override suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID {
        return transactionalOperator.executeAndAwait {
            val room = CallRoom(
                hostId = userId,
                title = request.title,
                maxParticipants = request.maxParticipantCount,
                currentParticipantsCount = 0,
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

    override suspend fun joinRoomCheckByCode(userId: UUID, roomCode: String): RoomInfoResponse {
        val room = callRoomRepository.findByRoomCode(roomCode) ?: throw RoomException.RoomNotFound()
        if(isRoomFull(room.id)) throw RoomException.RoomIsFull()

        return room.toRoomInfoResponse()
    }

    override suspend fun updateCurrentParticipantCount(roomId: UUID) {
        transactionalOperator.executeAndAwait {
            val count = callParticipantService.currentCountByRoomId(roomId)
            callRoomRepository.updateCurrentParticipantCount(roomId, count)
        }
    }

    override suspend fun isRoomFull(roomId: UUID): Boolean {
        val room = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
        val participantCount = callParticipantService.currentCountByRoomId(roomId)
        return participantCount >= room.maxParticipants
    }

    override suspend fun getRoomInfo(roomId: UUID): RoomInfoResponse {
        return callRoomRepository.findById(roomId)?.toRoomInfoResponse() ?: throw RoomException.RoomNotFound()
    }

    override suspend fun getOngoingRoomInfo(roomId: UUID): OngoingRoomInfoResponse {
        val room = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
        val participants = callParticipantService.findCurrentParticipants(roomId)
        return room.toOngoingRoomInfoResponse(participants)
    }

    override suspend fun getOngoingRoomList(
        createdAtSort: SortDirection,
        participantSort: SortDirection,
        pagination: CursorPagination
    ): CursorPage<OngoingRoomInfoResponse> {
        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                getOngoingRoomInfoResponse(participantSort, createdAtSort, cursor, limit)
            },
            convertItemToCursorFunc = {
                UUIDCursor(UUID.fromString(it.roomInfo.roomId))
            },
        )
    }

    override suspend fun getJanusRoomId(roomId: UUID): Long {
        return transactionalOperator.executeAndAwait {
            janusRoomIdCacheRepository.getJanusRoomId(roomId)?.let { cachedId ->
                return@executeAndAwait cachedId
            }

            val room = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
            return@executeAndAwait room.requireJanusRoomId().also {
                janusRoomIdCacheRepository.saveJanusRoomId(roomId, it, JANUS_ROOM_ID_CACHE_TTL)
            }
        }
    }

    override suspend fun updateRoomStatus(roomId: UUID) {
        transactionalOperator.executeAndAwait {
            val count = callParticipantService.currentCountByRoomId(roomId)
            val status = when(count) {
                0 -> RoomStatus.ENDED
                1 -> RoomStatus.WAITING
                else -> RoomStatus.IN_PROGRESS
            }
            callRoomRepository.updateRoomStatus(roomId, status)
        }
    }

    private suspend fun getOngoingRoomInfoResponse(
        participantSort: SortDirection,
        createdAtSort: SortDirection,
        cursor: UUIDCursor?,
        limit: Int
    ): List<OngoingRoomInfoResponse> {
        val roomList = callRoomRepository.findOngoingRoomList(participantSort, createdAtSort, cursor, limit)
        val roomIds = roomList.map { it.id }
        val currentParticipantsGroup = callParticipantService.findCurrentParticipantsGroupByRoomIds(roomIds)
        return roomList.map { roomInfo ->
            val participants = currentParticipantsGroup[roomInfo.id]
                ?.sortedByDescending { it.createdAt }
                ?: emptyList()

            roomInfo.toOngoingRoomInfoResponse(participants)
        }
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