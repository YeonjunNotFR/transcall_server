package com.youhajun.transcall.call.history.controller

import com.youhajun.transcall.call.history.dto.CallHistoryWithParticipantsResponse
import com.youhajun.transcall.call.history.service.CallHistoryService
import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.common.vo.SortDirection
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.vo.CursorPagination
import com.youhajun.transcall.pagination.vo.PagingDirection
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/call")
@Validated
class CallHistoryController(
    private val callHistoryService: CallHistoryService,
) {
    @GetMapping("/histories")
    suspend fun getCallHistoriesWithParticipants(
        authentication: Authentication,
        @RequestParam(required = false) after: String?,
        @Min(1) @RequestParam(defaultValue = "30") first: Int,
        @RequestParam(required = true) direction: String,
    ): CursorPage<CallHistoryWithParticipantsResponse> {
        val principal = authentication.principal as UserPrincipal
        val pagination = CursorPagination(after, first)
        return callHistoryService.getCallHistoriesWithParticipants(principal.userId, pagination, PagingDirection.fromType(direction))
    }

    @GetMapping("history/{historyId}")
    suspend fun getCallHistoryWithParticipants(
        authentication: Authentication,
        @NotBlank @PathVariable historyId: String,
    ): CallHistoryWithParticipantsResponse {
        val principal = authentication.principal as UserPrincipal
        val historyUUID = UUID.fromString(historyId)
        return callHistoryService.getCallHistoryWithParticipants(principal.userId, historyUUID)
    }
}