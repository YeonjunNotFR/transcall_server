package com.youhajun.transcall.client.janus.dto.video.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class VideoRoomResponseType(val type: String) {
    JOINED("joined"),
    ATTACHED("attached"),
    CREATED("created"),
    UPDATED("updated"),
    EVENT("event");

    @JsonValue
    override fun toString(): String = type


    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String?): VideoRoomResponseType? {
            return VideoRoomResponseType.entries.firstOrNull { it.type == type }
        }
    }
}