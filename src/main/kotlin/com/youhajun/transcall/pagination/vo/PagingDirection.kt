package com.youhajun.transcall.pagination.vo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class PagingDirection(val type: String) {
    NEXT("next"), PREVIOUS("previous");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): PagingDirection =
            entries.firstOrNull { it.type == type.lowercase() } ?: NEXT
    }
}