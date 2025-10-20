package com.youhajun.transcall.client.janus.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class JanusCommand(private val command: String) {
    CREATE("create"),
    DESTROY("destroy"),
    ATTACH("attach"),
    MESSAGE("message"),
    TRICKLE("trickle"),
    DETACH("detach"),
    HANGUP("hangup"),
    KEEPALIVE("keepalive");

    @JsonValue
    override fun toString() = command
}