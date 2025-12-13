package com.youhajun.transcall.call.room.service

import com.youhajun.transcall.call.participant.service.CallParticipantService
import com.youhajun.transcall.call.room.domain.CallRoom
import com.youhajun.transcall.call.room.domain.RoomJoinType
import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.call.room.dto.RoomInfoWithParticipantsResponse
import com.youhajun.transcall.call.room.exception.RoomException
import com.youhajun.transcall.call.room.repository.CallRoomRepository
import com.youhajun.transcall.call.room.repository.JanusRoomIdCacheRepository
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.janus.dto.JanusPlugin
import com.youhajun.transcall.janus.service.JanusRoomService
import com.youhajun.transcall.janus.service.JanusService
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
    private val janusRoomService: JanusRoomService,
    private val janusService: JanusService
) : CallRoomService {

    companion object {
        private val JANUS_ROOM_ID_CACHE_TTL = Duration.ofHours(24)
        private const val ROOM_CODE_LENGTH = 5
    }

    override suspend fun createRoom(userId: UUID, request: CreateRoomRequest): UUID {
        var sessionId: Long? = null
        return try {
            sessionId = janusService.createHttpSession().getOrThrow()
            val handleId = janusService.attachHttpPlugin(sessionId, JanusPlugin.VIDEO_ROOM.pkgName).getOrThrow()
            val janusRoomId = janusRoomService.createRoom(sessionId, handleId).getOrThrow()
            transactionalOperator.executeAndAwait {
                val room = CallRoom(
                    hostId = userId,
                    title = request.title,
                    maxParticipants = request.maxParticipantCount,
                    currentParticipantsCount = 0,
                    visibility = request.visibility,
                    tags = request.tags,
                    roomCode = generateUniqueRoomCode(),
                    janusRoomId = janusRoomId,
                    joinType = RoomJoinType.CODE_JOIN,
                )

                val saved = callRoomRepository.save(room)
                janusRoomIdCacheRepository.saveJanusRoomId(saved.id, janusRoomId, JANUS_ROOM_ID_CACHE_TTL)
                saved.id
            }
        } finally {
            sessionId?.let { janusService.destroyHttpSession(it) }
        }
    }

    override suspend fun getRoomInfoWithCurrentParticipants(roomId: UUID): RoomInfoWithParticipantsResponse {
        val roomInfo = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
        val participants = callParticipantService.getCurrentParticipants(roomId).map { it.toParticipantResponse() }
        return RoomInfoWithParticipantsResponse(roomInfo = roomInfo.toRoomInfoResponse(), participants = participants)
    }

    override suspend fun joinRoomCheckByCode(userId: UUID, roomCode: String): RoomInfoResponse {
        val room = callRoomRepository.findByRoomCode(roomCode) ?: throw RoomException.RoomNotFound()
        if(isRoomFull(room.id)) throw RoomException.RoomIsFull()

        return room.toRoomInfoResponse()
    }

    override suspend fun updateRoomStatusAndCount(roomId: UUID) {
        transactionalOperator.executeAndAwait {
            callRoomRepository.updateRoomStatusAndCount(roomId)
        }
    }

    override suspend fun isRoomFull(roomId: UUID): Boolean {
        val room = callRoomRepository.findById(roomId) ?: throw RoomException.RoomNotFound()
        val participantCount = callParticipantService.getCurrentCount(roomId)
        return participantCount >= room.maxParticipants
    }

    override suspend fun getRoomInfo(roomId: UUID): RoomInfoResponse {
        return callRoomRepository.findById(roomId)?.toRoomInfoResponse() ?: throw RoomException.RoomNotFound()
    }

    override suspend fun getRoomInfoWithCurrentParticipantsList(
        createdAtSort: SortDirection,
        participantSort: SortDirection,
        pagination: CursorPagination
    ): CursorPage<RoomInfoWithParticipantsResponse> {
        return CursorPaginationHelper.paginate(
            cursorPagination = pagination,
            codec = UUIDCursorCodec,
            fetchFunc = { cursor, limit ->
                callRoomRepository.findAllCurrentRoomInfoWithParticipants(participantSort, createdAtSort, cursor, limit)
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
            return@executeAndAwait room.janusRoomId.also {
                janusRoomIdCacheRepository.saveJanusRoomId(roomId, it, JANUS_ROOM_ID_CACHE_TTL)
            }
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