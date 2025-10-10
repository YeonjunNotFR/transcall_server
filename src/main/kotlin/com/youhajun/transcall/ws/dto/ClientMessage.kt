package com.youhajun.transcall.ws.dto

import com.youhajun.transcall.ws.dto.payload.RequestPayload
import com.youhajun.transcall.ws.vo.MessageType

data class ClientMessage(
    val type: MessageType,
    val payload: RequestPayload,
)