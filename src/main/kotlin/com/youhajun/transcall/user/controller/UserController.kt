package com.youhajun.transcall.user.controller

import com.youhajun.transcall.common.domain.UserPrincipal
import com.youhajun.transcall.user.dto.MyInfoResponse
import com.youhajun.transcall.user.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
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
        val userPublicId = principal.userPublicId
        return userService.getMyInfo(userPublicId)
    }
}