package com.youhajun.transcall.call.room.controller

import com.youhajun.transcall.call.room.dto.CreateRoomRequest
import com.youhajun.transcall.call.room.service.CallRoomService
import com.youhajun.transcall.common.domain.UserPrincipal
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
}