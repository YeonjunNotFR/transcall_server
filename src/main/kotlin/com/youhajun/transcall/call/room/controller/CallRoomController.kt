package com.youhajun.transcall.call.room.controller

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.OngoingRoomInfoResponse
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
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
    suspend fun getRoomInfo(
        @NotBlank @PathVariable roomId: String,
    ): RoomInfoResponse {
        return callRoomService.getRoomInfo(UUID.fromString(roomId))
    }

    @GetMapping("/{roomId}/ongoing")
    suspend fun getOngoingRoomInfo(
        @NotBlank @PathVariable roomId: String,
    ): OngoingRoomInfoResponse {
        return callRoomService.getOngoingRoomInfo(UUID.fromString(roomId))
    }

    @GetMapping("/list")
    suspend fun getRoomList(
        @RequestParam(defaultValue = "DESC") createdAtSort: SortDirection,
        @RequestParam(defaultValue = "DESC") participantSort: SortDirection,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
    ): CursorPage<OngoingRoomInfoResponse> {
        val pagination = CursorPagination(after, first)
        return callRoomService.getOngoingRoomList(createdAtSort, participantSort, pagination)
    }
}