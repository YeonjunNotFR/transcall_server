package com.youhajun.transcall.call.room.domain

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RoomStatus(val type: String) {
    WAITING("waiting"),
    IN_PROGRESS("in_progress"),
    ENDED("ended");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): RoomStatus =
            RoomStatus.entries.firstOrNull { it.type == type.lowercase() } ?: throw IllegalArgumentException("Invalid RoomType: $type")
    }
}