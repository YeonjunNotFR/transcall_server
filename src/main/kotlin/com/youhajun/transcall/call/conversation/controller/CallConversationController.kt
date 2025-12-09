package com.youhajun.transcall.call.conversation.controller

import com.youhajun.transcall.call.conversation.dto.ConversationResponse
import com.youhajun.transcall.call.conversation.service.CallConversationService
import com.youhajun.transcall.call.history.service.CallHistoryService
import com.youhajun.transcall.call.participant.service.CallParticipantService
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
@RequestMapping("/api/call/conversations")
@Validated
class CallConversationController(
    private val callConversationService: CallConversationService,
    private val callHistoryService: CallHistoryService,
    private val callParticipantService: CallParticipantService,
) {

    @GetMapping("/{historyId}")
    suspend fun getConversationsInHistory(
        authentication: Authentication,
        @NotBlank @PathVariable historyId: String,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
    ): CursorPage<ConversationResponse> {
        val principal = authentication.principal as UserPrincipal
        val rangeContext = getRangeContext(principal.userId, historyId)
        val pagination = CursorPagination(after, first)
        return callConversationService.getConversationsInTimeRange(principal.userId, rangeContext.roomUUID, rangeContext.timeRange, pagination)
    }

    @GetMapping("/{historyId}/sync")
    suspend fun getConversationsSyncInHistory(
        authentication: Authentication,
        @NotBlank @PathVariable historyId: String,
        @RequestParam(required = true) updatedAfter: Long,
    ): CursorPage<ConversationResponse> {
        val principal = authentication.principal as UserPrincipal
        val rangeContext = getRangeContext(principal.userId, historyId)
        return callConversationService.getConversationsSyncInTimeRange(principal.userId, rangeContext.roomUUID, rangeContext.timeRange, updatedAfter)
    }

    private suspend fun getRangeContext(userId: UUID, historyId: String): RangeContext {
        val history = callHistoryService.getCallHistory(userId, UUID.fromString(historyId))
        val roomUUID = UUID.fromString(history.roomId)

        callParticipantService.checkParticipant(roomUUID, userId)

        return RangeContext(
            roomUUID = roomUUID,
            timeRange = TimeRange.fromEpochTime(history.createdAt, history.leftAt)
        )
    }

    private data class RangeContext(val roomUUID: UUID, val timeRange: TimeRange)
}