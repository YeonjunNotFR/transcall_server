package com.youhajun.transcall.call.conversation.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ConversationState(val type: String) {
    PENDING("pending"), FINAL("final");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): ConversationState =
            entries.firstOrNull { it.type == type.lowercase() } ?: FINAL
    }
}