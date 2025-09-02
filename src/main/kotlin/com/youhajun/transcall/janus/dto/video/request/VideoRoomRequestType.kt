package com.youhajun.transcall.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonValue

enum class VideoRoomRequestType(val value: String) {
    CREATE("create"),
    JOIN("join"),
    PUBLISH("publish"),
    UPDATE("update"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    START("start");

    @JsonValue
    override fun toString(): String = value
}