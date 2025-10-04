package com.youhajun.transcall.call.conversation.controller

import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.service.CallConversationService
import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.common.vo.TimeRange
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/call/room")
@Validated
class CallConversationController(
    private val callConversationService: CallConversationService
) {

    @GetMapping("/{roomId}/conversations")
    suspend fun getConversationsInTimeRange(
        authentication: Authentication,
        @NotBlank @PathVariable roomId: String,
        @RequestParam(required = true) joinedAtToEpochTime: Long,
        @RequestParam(required = false) leftAtToEpochTime: Long?,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
    ): CursorPage<ConversationResponse> {
        val principal = authentication.principal as UserPrincipal
        val roomUUID = UUID.fromString(roomId)
        val timeRange = TimeRange.fromEpochTime(joinedAtToEpochTime, leftAtToEpochTime)
        val pagination = CursorPagination(after, first)
        return callConversationService.getConversationsInTimeRange(principal.userId, roomUUID, timeRange, pagination)
    }

    @GetMapping("/{roomId}/conversations/sync")
    suspend fun getConversationsSyncTimeRange(
        authentication: Authentication,
        @NotBlank @PathVariable roomId: String,
        @RequestParam(required = true) joinedAtToEpochTime: Long,
        @RequestParam(required = false) leftAtToEpochTime: Long?,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
        @RequestParam(required = false) updatedAfter: Long?,
    ): CursorPage<ConversationResponse> {
        val principal = authentication.principal as UserPrincipal
        val roomUUID = UUID.fromString(roomId)
        val timeRange = TimeRange.fromEpochTime(joinedAtToEpochTime, leftAtToEpochTime)
        val pagination = CursorPagination(after, first)
        return callConversationService.getConversationsSyncTimeRange(principal.userId, roomUUID, timeRange, pagination, updatedAfter)
    }

}