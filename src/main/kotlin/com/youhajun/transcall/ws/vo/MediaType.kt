package com.youhajun.transcall.ws.vo

import com.fasterxml.jackson.annotation.JsonCreator

enum class MediaType(val type: String) {
    AUDIO("audio"),
    VIDEO("video");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): MediaType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}