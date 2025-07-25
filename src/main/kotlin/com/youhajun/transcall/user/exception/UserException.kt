package com.youhajun.transcall.user.exception

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class UserException(errorType: ErrorType): TransCallException(errorType) {
    class UserNotFoundException : UserException(ErrorType.USER_NOT_FOUND)
    class UserQuotaNotFoundException : UserException(ErrorType.USER_QUOTA_NOT_FOUND)
}