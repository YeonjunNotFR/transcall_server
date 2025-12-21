package com.youhajun.transcall.user.controller

import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.user.dto.MyInfoResponse
import com.youhajun.transcall.user.domain.UserSettings
import com.youhajun.transcall.user.service.UserService
import com.youhajun.transcall.user.vo.UserTermsUpdateRequestVo
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users/")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/me")
    suspend fun getMyInfo(authentication: Authentication): MyInfoResponse {
        val principal = authentication.principal as UserPrincipal
        return userService.getMyInfo(principal.userId)
    }

    @PatchMapping("/me/settings")
    suspend fun updateMyTermsSettings(
        authentication: Authentication,
        @RequestBody request: UserTermsUpdateRequestVo
    ): UserSettings {
        val principal = authentication.principal as UserPrincipal
        return userService.updateUserTerms(principal.userId, request)
    }
}
