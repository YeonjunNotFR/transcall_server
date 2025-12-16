package com.youhajun.transcall.ws.exception

import org.springframework.web.reactive.socket.CloseStatus

sealed class WebSocketException(val closeStatus: CloseStatus) : RuntimeException() {
    class MissingRoomId : WebSocketException(CloseStatus.BAD_DATA)
    class MissingToken : WebSocketException(CloseStatus.BAD_DATA)
    class RoomFull : WebSocketException(CloseStatus.POLICY_VIOLATION)
    class InvalidToken : WebSocketException(CloseStatus.POLICY_VIOLATION)
    class SessionNotFound : WebSocketException(CloseStatus.BAD_DATA)
}