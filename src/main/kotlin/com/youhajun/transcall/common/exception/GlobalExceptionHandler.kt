package com.youhajun.transcall.common.exception

import com.youhajun.transcall.common.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TransCallException::class)
    fun handleTransCallException(e: TransCallException, req: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(e.errorType.httpStatus).body(
            ErrorResponse(
                status = e.errorType.status,
                message = e.message,
                data = e.data
            )
        )
    }
}