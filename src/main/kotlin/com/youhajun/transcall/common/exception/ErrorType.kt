package com.youhajun.transcall.common.exception

import org.springframework.http.HttpStatus

enum class ErrorType(val httpStatus: HttpStatus, val status: String, val message: String) {
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_EXPIRED", "JWT 토큰이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_ACCESS_TOKEN", "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_GOOGLE_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_TOKEN", "유효하지 않은 구글 토큰입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "이메일이 인증되지 않았습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    USER_QUOTA_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_QUOTA_NOT_FOUND", "사용자 할당량을 찾을 수 없습니다."),
    NONCE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NONCE_SAVE_FAILED", "Nonce 저장에 실패했습니다."),
    NONCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NONCE_NOT_FOUND", "Nonce를 찾을 수 없습니다."),
    FORBIDDEN_CALL_NOT_JOIN(HttpStatus.FORBIDDEN, "FORBIDDEN_CALL_NOT_JOIN", "참여하지 않은 통화입니다."),
    CALL_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CALL_HISTORY_NOT_FOUND", "통화 기록을 찾을 수 없습니다."),
    ROOM_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ROOM_CODE_GENERATION_FAILED", "방 코드 생성에 실패했습니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다."),
    ROOM_IS_FULL(HttpStatus.FORBIDDEN, "ROOM_IS_FULL", "방이 가득 찼습니다."),
}