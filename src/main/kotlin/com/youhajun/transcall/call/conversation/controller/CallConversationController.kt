package com.youhajun.transcall.call.conversation.controller

import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.service.CallConversationService
import com.youhajun.transcall.pagination.dto.CursorPage
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/call")
class CallConversationController(
    private val callConversationService: CallConversationService
) {
    @GetMapping("/{roomCode}/conversations")
    suspend fun getConversations(
        authentication: Authentication,
        @PathVariable roomCode: UUID,
        @RequestParam(required = false) after: String?,
        @RequestParam(defaultValue = "30") first: Int,
    ): CursorPage<ConversationResponse> {
        val principal = authentication.principal as UserPrincipal
        val userPublicId = principal.userPublicId
        return callConversationService.getCallConversations(userPublicId, roomCode, after, first)
    }
}