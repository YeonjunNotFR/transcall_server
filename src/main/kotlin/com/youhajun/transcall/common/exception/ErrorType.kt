package com.youhajun.transcall.common.exception

import org.springframework.http.HttpStatus

enum class ErrorType(val httpStatus: HttpStatus, val status: String, val message: String) {
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_EXPIRED", "JWT 토큰이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_GOOGLE_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_TOKEN", "유효하지 않은 구글 토큰입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "EMAIL_NOT_VERIFIED", "이메일이 인증되지 않았습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    NONCE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NONCE_SAVE_FAILED", "Nonce 저장에 실패했습니다."),
    NONCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NONCE_NOT_FOUND", "Nonce를 찾을 수 없습니다."),
}