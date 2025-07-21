package com.youhajun.transcall.auth.domain

import java.util.*

data class UserPrincipal(
    val userPublicId: UUID,
    val plan: String
)
