package com.youhajun.transcall.janus.exception

import com.youhajun.transcall.janus.dto.JanusError
import org.springframework.web.reactive.socket.CloseStatus

sealed class JanusException(val closeStatus: CloseStatus) : RuntimeException() {
    class JanusResponseException(response: JanusError) : JanusException(CloseStatus.SERVER_ERROR) {
        val code = response.code
        val reason = response.reason
    }

    class JanusResponseMappingException : JanusException(CloseStatus.SERVER_ERROR)

    class JanusConnectionException : JanusException(CloseStatus.SERVER_ERROR)
    class JanusTimeoutException : JanusException(CloseStatus.SERVER_ERROR)
}