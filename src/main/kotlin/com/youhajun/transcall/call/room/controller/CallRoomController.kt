package com.youhajun.transcall.call.room.controller

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.dto.RoomInfoResponse
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.common.domain.UserPrincipal
import jakarta.validation.constraints.NotBlank
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

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

    @GetMapping("/join")
    suspend fun joinRoomByCode(
        authentication: Authentication,
        @RequestParam @NotBlank roomCode: String,
    ): RoomInfoResponse {
        val principal = authentication.principal as UserPrincipal
        return callRoomService.joinRoomByCode(principal.userId, roomCode)
    }
}