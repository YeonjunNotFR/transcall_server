package com.youhajun.transcall.call.room.controller

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.call.room.dto.RoomInfoWithParticipantsResponse
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("api/room")
class CallRoomController(
    private val callRoomService: CallRoomService
) {
    @PostMapping("/create")
    suspend fun createRoom(
        authentication: Authentication,
        @RequestBody request: CreateRoomRequest
    ): String {
        val principal = authentication.principal as UserPrincipal
        return callRoomService.createRoom(principal.userId, request).toString()
    }

    @GetMapping("/join-check")
    suspend fun joinRoomCheckByCode(
        authentication: Authentication,
        @RequestParam @NotBlank roomCode: String,
    ): RoomInfoResponse {
        val principal = authentication.principal as UserPrincipal
        return callRoomService.joinRoomCheckByCode(principal.userId, roomCode)
    }

    @GetMapping("/{roomId}")
    suspend fun getRoomInfoWithCurrentParticipants(
        @NotBlank @PathVariable roomId: String,
    ): RoomInfoWithParticipantsResponse {
        return callRoomService.getRoomInfoWithCurrentParticipants(UUID.fromString(roomId))
    }

    @GetMapping("/list")
    suspend fun getRoomInfoWithCurrentParticipantsList(
        @RequestParam(required = true) createdAtSort: String,
        @RequestParam(required = true) participantSort: String,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
    ): CursorPage<RoomInfoWithParticipantsResponse> {
        val pagination = CursorPagination(after, first)
        return callRoomService.getRoomInfoWithCurrentParticipantsList(
            SortDirection.fromType(createdAtSort),
            SortDirection.fromType(participantSort),
            pagination
        )
    }
}