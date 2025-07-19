package com.youhajun.transcall.user.domain

import com.fasterxml.jackson.annotation.JsonCreator

enum class SocialType(val type: String) {
    GOOGLE("google");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): SocialType =
            entries.firstOrNull { it.type == type.lowercase() } ?: throw IllegalArgumentException("Invalid SocialType: $type")
    }
}