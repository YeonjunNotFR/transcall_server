package com.youhajun.transcall.pagination.cursor

interface Cursor

interface CursorCodec<T: Cursor> {
    fun encode(cursor: T): String
    fun decode(raw: String): T
}