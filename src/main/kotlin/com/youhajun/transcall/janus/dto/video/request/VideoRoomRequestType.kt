package com.youhajun.transcall.janus.dto.video.request

import com.fasterxml.jackson.annotation.JsonValue

enum class VideoRoomRequestType(val value: String) {
    CREATE("create"),
    JOIN("join"),
    PUBLISH("publish"),
    UNPUBLISH("unpublish"),
    LEAVE("leave"),
    UPDATE("update"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    START("start");

    @JsonValue
    override fun toString(): String = value
}