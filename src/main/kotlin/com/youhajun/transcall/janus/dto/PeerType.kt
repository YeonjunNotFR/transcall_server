package com.youhajun.transcall.janus.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class PeerType(val type: String) {
    PUBLISHER("publisher"),
    SUBSCRIBER("subscriber");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): PeerType? = entries.firstOrNull { it.type == type.lowercase() }
    }
}