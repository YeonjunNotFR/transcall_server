package com.youhajun.transcall.call.history.controller

import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.call.history.dto.CallHistoryResponse
import com.youhajun.transcall.call.history.service.CallHistoryService
import com.youhajun.transcall.pagination.dto.CursorPage
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/call")
class CallHistoryController(
    private val callHistoryService: CallHistoryService,
) {
    @GetMapping("/histories")
    suspend fun getCallHistories(
        authentication: Authentication,
        @RequestParam(required = false) after: String?,
        @RequestParam(defaultValue = "30") first: Int,

    ): CursorPage<CallHistoryResponse> {
        val principal = authentication.principal as UserPrincipal
        return callHistoryService.getCallHistories(principal.userPublicId, after, first)
    }
}