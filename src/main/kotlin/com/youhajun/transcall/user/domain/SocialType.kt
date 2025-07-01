package com.youhajun.transcall.user.domain

enum class SocialType(val type: String) {
    GOOGLE("google");

    companion object {
        fun fromType(type: String): SocialType =
            entries.firstOrNull { it.type == type } ?: GOOGLE
    }
}