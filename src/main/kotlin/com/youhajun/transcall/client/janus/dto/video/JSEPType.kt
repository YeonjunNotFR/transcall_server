package com.youhajun.transcall.client.janus.dto.video

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class JSEPType(val type: String) {
    OFFER("offer"),
    ANSWER("answer");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): JSEPType? = entries.firstOrNull { it.type == type.lowercase() }
    }
}