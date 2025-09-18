package com.youhajun.transcall.call.room.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RoomVisibility(val type: String) {
    PUBLIC("public"),
    PRIVATE("private");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): RoomVisibility =
            RoomVisibility.entries.firstOrNull { it.type == type.lowercase() } ?: throw IllegalArgumentException("Invalid RoomVisibility: $type")
    }
}