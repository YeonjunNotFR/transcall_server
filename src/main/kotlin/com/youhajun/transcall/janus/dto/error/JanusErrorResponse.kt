package com.youhajun.transcall.janus.dto.error

import com.youhajun.transcall.janus.dto.BaseJanusResponse
import com.youhajun.transcall.janus.dto.JanusResponseType

data class JanusErrorResponse(
    val transaction: String?,
    val error: JanusError
) : BaseJanusResponse {
    override val janus: JanusResponseType = JanusResponseType.ERROR
}

data class JanusError(
    val code: Int,
    val reason: String
)