package com.youhajun.transcall.ws.vo

import com.fasterxml.jackson.annotation.JsonCreator

enum class MessageType(val type: String) {
    SIGNALING("signaling"),
    TRANSLATION("translation"),
    MEDIA_STATE("mediaState"),
    ROOM("room");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): MessageType? {
            return entries.firstOrNull { it.type == type }
        }
    }
}