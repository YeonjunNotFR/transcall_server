package com.youhajun.transcall.auth.exception

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class AuthException(errorType: ErrorType): TransCallException(errorType) {
    class JwtExpiredException : AuthException(ErrorType.JWT_EXPIRED)
    class InvalidAccessTokenException : AuthException(ErrorType.INVALID_ACCESS_TOKEN)
    class InvalidRefreshTokenException : AuthException(ErrorType.INVALID_REFRESH_TOKEN)
    class InvalidGoogleTokenException : AuthException(ErrorType.INVALID_GOOGLE_TOKEN)
    class EmailNotVerifiedException : AuthException(ErrorType.EMAIL_NOT_VERIFIED)
    class NonceSaveFailedException : AuthException(ErrorType.NONCE_SAVE_FAILED)
    class NonceNotFoundException : AuthException(ErrorType.NONCE_NOT_FOUND)
}