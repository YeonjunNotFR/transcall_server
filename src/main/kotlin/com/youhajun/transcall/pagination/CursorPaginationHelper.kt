package com.youhajun.transcall.pagination

import com.youhajun.transcall.pagination.cursor.Cursor
import com.youhajun.transcall.pagination.cursor.CursorCodec
import com.youhajun.transcall.pagination.dto.CursorPage
import com.youhajun.transcall.pagination.dto.Node
import com.youhajun.transcall.pagination.dto.PageInfo

object CursorPaginationHelper {

    suspend fun <T, C: Cursor> paginate(
        first: Int,
        after: String?,
        codec: CursorCodec<C>,
        fetchFunc: suspend (cursor: C?, limit: Int) -> List<T>,
        convertItemToCursorFunc: (T) -> C,
        totalCountFunc: (suspend () -> Long)? = null
    ): CursorPage<T> {
        val decodedCursor = after?.takeIf { it.isNotBlank() }?.let(codec::decode)

        val items = fetchFunc(decodedCursor, first + 1)
        val hasNextPage = items.size > first
        val pageItems = items.take(first)

        val edges = pageItems.map {
            val encodedCursor = codec.encode(convertItemToCursorFunc(it))
            Node(node = it, cursor = encodedCursor)
        }

        val totalCount = totalCountFunc?.invoke()
        val nextCursor = edges.lastOrNull()?.cursor

        return CursorPage(
            edges = edges,
            pageInfo = PageInfo(
                hasNextPage = hasNextPage,
                nextCursor = nextCursor ?: ""
            ),
            totalCount = totalCount ?: -1
        )
    }
}