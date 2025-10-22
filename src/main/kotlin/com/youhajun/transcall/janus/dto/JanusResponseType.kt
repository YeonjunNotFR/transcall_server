package com.youhajun.transcall.janus.dto;

import com.fasterxml.jackson.annotation.JsonCreator

enum class JanusResponseType(val value: String) {
    SUCCESS("success"),
    ACK("ack"),
    EVENT("event"),
    ERROR("error"),
    TRICKLE("trickle"),
    WEBRTCUP("webrtcup"),
    HANGUP("hangup"),
    DETACHED("detached"),
    MEDIA("media"),
    SLOWLINK("slowlink"),
    TIMEOUT("timeout");

    override fun toString(): String = value

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(value: String?): JanusResponseType? = entries.firstOrNull { it.value == value }
    }
}