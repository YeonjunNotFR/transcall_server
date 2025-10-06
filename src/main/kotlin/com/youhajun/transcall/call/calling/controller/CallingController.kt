package com.youhajun.transcall.call.calling.controller

import com.youhajun.transcall.call.calling.dto.TurnCredential
import com.youhajun.transcall.call.calling.service.CallingService
import com.youhajun.transcall.common.domain.UserPrincipal
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/call")
@Validated
class CallingController(
    private val callingService: CallingService
) {
    @GetMapping("/turn-credential")
    suspend fun getTurnCredential(authentication: Authentication): TurnCredential {
        val principal = authentication.principal as UserPrincipal
        return callingService.getTurnCredential(principal.userId)
    }
}