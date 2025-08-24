package com.youhajun.transcall.call.room.domain

import com.fasterxml.jackson.annotation.JsonCreator

enum class RoomJoinType(val type: String) {
    CODE_JOIN("code"),
    DIRECT_CALL("direct");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): RoomJoinType? =
            RoomJoinType.entries.firstOrNull { it.type == type.lowercase() }
    }
}