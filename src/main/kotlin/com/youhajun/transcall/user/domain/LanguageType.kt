package com.youhajun.transcall.user.domain

import com.fasterxml.jackson.annotation.JsonCreator

enum class LanguageType(val code: String) {
    ENGLISH("en"),
    KOREAN("ko"),
    JAPANESE("ja"),
    CHINESE("zh"),
    SPANISH("es");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromCode(code: String): LanguageType =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}