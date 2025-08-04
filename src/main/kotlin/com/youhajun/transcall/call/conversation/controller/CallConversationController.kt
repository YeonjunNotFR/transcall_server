package com.youhajun.transcall.call.conversation.controller

import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.service.CallConversationService
import com.youhajun.transcall.pagination.dto.CursorPage
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/call")
@Validated
class CallConversationController(
    private val callConversationService: CallConversationService
) {
    @GetMapping("/{roomCode}/conversations")
    suspend fun getConversations(
        authentication: Authentication,
        @NotBlank @PathVariable roomCode: UUID,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
    ): CursorPage<ConversationResponse> {
        val principal = authentication.principal as UserPrincipal
        val userPublicId = principal.userPublicId
        return callConversationService.getCallConversations(userPublicId, roomCode, after, first)
    }
}