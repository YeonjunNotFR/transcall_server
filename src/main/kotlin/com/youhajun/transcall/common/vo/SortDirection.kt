package com.youhajun.transcall.common.vo

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class SortDirection(val type: String) {
    ASC("ASC"),
    DESC("DESC");

    @JsonValue
    override fun toString(): String = type

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromType(type: String): SortDirection =
            entries.firstOrNull { it.type == type.lowercase() } ?: DESC
    }
}