package com.youhajun.transcall.ws.handler

import com.youhajun.transcall.ws.dto.ClientMessage
import com.youhajun.transcall.ws.vo.MessageType
import java.util.*

interface WebSocketMessageHandler {
    fun supports(type: MessageType): Boolean

    suspend fun handle(userId: UUID, roomId: UUID, msg: ClientMessage)
}