package com.youhajun.transcall.ws.vo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MediaContentType(val type: String) {
    DEFAULT("default"),
    SCREEN_SHARE("screenShare");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): MediaContentType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}