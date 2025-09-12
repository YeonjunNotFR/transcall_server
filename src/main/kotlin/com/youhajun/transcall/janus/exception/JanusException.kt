package com.youhajun.transcall.janus.exception

import com.youhajun.transcall.janus.dto.error.JanusErrorResponse
import org.springframework.web.reactive.socket.CloseStatus

sealed class JanusException(val closeStatus: CloseStatus) : RuntimeException() {
    class JanusResponseException(response: JanusErrorResponse) : JanusException(CloseStatus.SERVER_ERROR) {
        val code = response.error.code
        val reason = response.error.reason
    }

    class JanusResponseMappingException : JanusException(CloseStatus.SERVER_ERROR)

    class JanusConnectionException : JanusException(CloseStatus.SERVER_ERROR)
}