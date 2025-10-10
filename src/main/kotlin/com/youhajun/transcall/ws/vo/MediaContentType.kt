package com.youhajun.transcall.ws.vo

import com.fasterxml.jackson.annotation.JsonCreator

enum class MediaContentType(val type: String) {
    DEFAULT("default"),
    SCREEN_SHARE("screenShare");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): MediaContentType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}