package com.youhajun.transcall.call.room.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RoomJoinType(val type: String) {
    CODE_JOIN("code"),
    DIRECT_CALL("direct");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): RoomJoinType? =
            RoomJoinType.entries.firstOrNull { it.type == type.lowercase() }
    }
}