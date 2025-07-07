package com.youhajun.transcall.auth.exception

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class AuthException(errorType: ErrorType): TransCallException(errorType) {
    class JwtExpiredException : AuthException(ErrorType.JWT_EXPIRED)
    class InvalidAccessTokenException : AuthException(ErrorType.INVALID_ACCESS_TOKEN)
    class InvalidRefreshTokenException : AuthException(ErrorType.INVALID_REFRESH_TOKEN)
}