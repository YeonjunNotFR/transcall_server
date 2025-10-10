package com.youhajun.transcall.ws.dto

import com.youhajun.transcall.ws.dto.payload.ResponsePayload
import com.youhajun.transcall.ws.vo.MessageType
import java.util.*

data class ServerMessage(
    val type: MessageType,
    val payload: ResponsePayload,
    val timestamp: Long = System.currentTimeMillis(),
    val senderId: UUID? = null,
)